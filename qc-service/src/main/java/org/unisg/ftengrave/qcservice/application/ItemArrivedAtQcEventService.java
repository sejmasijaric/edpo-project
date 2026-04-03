package org.unisg.ftengrave.qcservice.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.qcservice.adapter.in.kafka.dto.SortingMachineEventDto;
import org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.MessageCorrelationService;
import org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.dto.CamundaMessageDto;
import org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.dto.MessageProcessDto;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemArrivedAtQcEventService {

    static final String ITEM_ARRIVED_AT_QC_MESSAGE = "ItemArrivedAtQC";
    private static final String ITEM_ARRIVED_AT_QC_EVENT = "item-arrived-at-qc";

    private final WaitingMessageBusinessKeyResolver waitingMessageBusinessKeyResolver;
    private final MessageCorrelationService messageCorrelationService;

    public void handle(SortingMachineEventDto event) {
        if (!isItemArrivedAtQcEvent(event)) {
            return;
        }

        String itemIdentifier = waitingMessageBusinessKeyResolver.resolve(ITEM_ARRIVED_AT_QC_MESSAGE);
        if (itemIdentifier == null) {
            return;
        }

        CamundaMessageDto message = CamundaMessageDto.builder()
                .dto(MessageProcessDto.builder()
                        .itemIdentifier(itemIdentifier)
                        .build())
                .build();

        messageCorrelationService.correlateMessage(message, ITEM_ARRIVED_AT_QC_MESSAGE);
    }

    private boolean isItemArrivedAtQcEvent(SortingMachineEventDto event) {
        if (event == null || event.getEventType() == null) {
            log.info("Ignoring sorting-machine event without eventType");
            return false;
        }

        if (!ITEM_ARRIVED_AT_QC_EVENT.equals(event.getEventType())) {
            log.info("Ignoring unsupported sorting-machine event {}", event.getEventType());
            return false;
        }

        return true;
    }
}
