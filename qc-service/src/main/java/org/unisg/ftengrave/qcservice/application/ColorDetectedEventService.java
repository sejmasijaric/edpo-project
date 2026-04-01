package org.unisg.ftengrave.qcservice.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.qcservice.adapter.in.kafka.dto.SortingMachineEventDto;
import org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.MessageCorrelationService;
import org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.dto.CamundaMessageDto;
import org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.dto.ColorDetectedMessageProcessDto;
import org.unisg.ftengrave.qcservice.domain.ItemColor;

@Service
@RequiredArgsConstructor
@Slf4j
public class ColorDetectedEventService {

    static final String COLOR_DETECTED_MESSAGE = "ColorDetectedMessage";
    private static final String DETECTED_COLOR_EVENT_PREFIX = "detected-color-";

    private final RuntimeService runtimeService;
    private final MessageCorrelationService messageCorrelationService;

    public void handle(SortingMachineEventDto event) {
        ItemColor color = mapDetectedColor(event);
        if (color == null) {
            return;
        }

        String itemIdentifier = resolveWaitingItemIdentifier();
        if (itemIdentifier == null) {
            return;
        }

        CamundaMessageDto message = CamundaMessageDto.builder()
                .dto(ColorDetectedMessageProcessDto.builder()
                        .itemIdentifier(itemIdentifier)
                        .color(color)
                        .build())
                .build();


        messageCorrelationService.correlateMessage(message, COLOR_DETECTED_MESSAGE);
    }

    private ItemColor mapDetectedColor(SortingMachineEventDto event) {
        if (event == null || event.getEventType() == null) {
            log.info("Ignoring sorting-machine event without eventType");
            return null;
        }

        return switch (event.getEventType()) {
            case DETECTED_COLOR_EVENT_PREFIX + "white" -> ItemColor.WHITE;
            case DETECTED_COLOR_EVENT_PREFIX + "red" -> ItemColor.RED;
            case DETECTED_COLOR_EVENT_PREFIX + "blue" -> ItemColor.BLUE;
            default -> {
                log.info("Ignoring unsupported sorting-machine event {}", event.getEventType());
                yield null;
            }
        };
    }

    private String resolveWaitingItemIdentifier() {
        try {
            Execution execution = runtimeService.createExecutionQuery()
                    .messageEventSubscriptionName(COLOR_DETECTED_MESSAGE)
                    .singleResult();

            if (execution == null) {
                log.warn("Ignoring detected color event because no process is waiting for {}", COLOR_DETECTED_MESSAGE);
                return null;
            }

            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(execution.getProcessInstanceId())
                    .singleResult();

            if (processInstance == null || processInstance.getBusinessKey() == null || processInstance.getBusinessKey().isBlank()) {
                log.warn("Ignoring detected color event because the waiting process instance has no business key");
                return null;
            }

            return processInstance.getBusinessKey();
        } catch (Exception exception) {
            log.warn("Ignoring detected color event because waiting process resolution is ambiguous", exception);
            return null;
        }
    }
}
