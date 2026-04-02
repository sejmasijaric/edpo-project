package org.unisg.ftengrave.qcservice.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private static final String COLOR_DETECTED_EVENT = "color-detected";

    private final WaitingMessageBusinessKeyResolver waitingMessageBusinessKeyResolver;
    private final MessageCorrelationService messageCorrelationService;

    public void handle(SortingMachineEventDto event) {
        ItemColor color = mapDetectedColor(event);
        if (color == null) {
            return;
        }

        String itemIdentifier = waitingMessageBusinessKeyResolver.resolve(COLOR_DETECTED_MESSAGE);
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

        if (!COLOR_DETECTED_EVENT.equals(event.getEventType())) {
            log.info("Ignoring unsupported sorting-machine event {}", event.getEventType());
            return null;
        }

        if (event.getColor() == null || event.getColor().isBlank()) {
            log.info("Ignoring color-detected event without color payload");
            return null;
        }

        return switch (event.getColor().trim().toLowerCase()) {
            case "white" -> ItemColor.WHITE;
            case "red" -> ItemColor.RED;
            case "blue" -> ItemColor.BLUE;
            default -> {
                log.info("Ignoring color-detected event with unsupported color {}", event.getColor());
                yield null;
            }
        };
    }
}
