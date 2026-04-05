package org.unisg.ftengrave.qcservice.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.qcservice.port.in.HandleItemArrivedAtQcEventUseCase;
import org.unisg.ftengrave.qcservice.port.in.SortingMachineEvent;
import org.unisg.ftengrave.qcservice.port.out.CorrelateMessagePort;
import org.unisg.ftengrave.qcservice.port.out.ResolveWaitingMessageBusinessKeyPort;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemArrivedAtQcEventService implements HandleItemArrivedAtQcEventUseCase {

    static final String ITEM_ARRIVED_AT_QC_MESSAGE = "ItemArrivedAtQC";
    private static final String ITEM_ARRIVED_AT_QC_EVENT = "item-arrived-at-qc";

    private final ResolveWaitingMessageBusinessKeyPort resolveWaitingMessageBusinessKeyPort;
    private final CorrelateMessagePort correlateMessagePort;

    @Override
    public void handle(SortingMachineEvent event) {
        if (!isItemArrivedAtQcEvent(event)) {
            return;
        }

        String itemIdentifier = resolveWaitingMessageBusinessKeyPort.resolve(ITEM_ARRIVED_AT_QC_MESSAGE);
        if (itemIdentifier == null) {
            return;
        }

        correlateMessagePort.correlateMessage(
                ITEM_ARRIVED_AT_QC_MESSAGE,
                itemIdentifier,
                Map.of("itemIdentifier", itemIdentifier));
    }

    private boolean isItemArrivedAtQcEvent(SortingMachineEvent event) {
        if (event == null || event.eventType() == null) {
            log.info("Ignoring sorting-machine event without eventType");
            return false;
        }

        if (!ITEM_ARRIVED_AT_QC_EVENT.equals(event.eventType())) {
            log.info("Ignoring unsupported sorting-machine event {}", event.eventType());
            return false;
        }

        return true;
    }
}
