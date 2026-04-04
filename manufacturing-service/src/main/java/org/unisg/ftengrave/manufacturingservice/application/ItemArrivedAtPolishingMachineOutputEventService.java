package org.unisg.ftengrave.manufacturingservice.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.manufacturingservice.port.in.HandleItemArrivedAtPolishingMachineOutputEventUseCase;
import org.unisg.ftengrave.manufacturingservice.port.in.PolishingMachineEvent;
import org.unisg.ftengrave.manufacturingservice.port.out.CorrelateMessagePort;
import org.unisg.ftengrave.manufacturingservice.port.out.ResolveWaitingMessageBusinessKeyPort;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemArrivedAtPolishingMachineOutputEventService implements HandleItemArrivedAtPolishingMachineOutputEventUseCase {

    static final String ITEM_ARRIVED_AT_POLISHING_MACHINE_OUTPUT_MESSAGE = "ItemArrivedPolishingOutputMessage";
    private static final String ITEM_ARRIVED_AT_POLISHING_MACHINE_OUTPUT_EVENT = "item-arrived-at-polishing-machine-output";

    private final ResolveWaitingMessageBusinessKeyPort resolveWaitingMessageBusinessKeyPort;
    private final CorrelateMessagePort correlateMessagePort;

    @Override
    public void handle(PolishingMachineEvent event) {
        if (!ITEM_ARRIVED_AT_POLISHING_MACHINE_OUTPUT_EVENT.equals(event == null ? null : event.eventType())) {
            log.info("Ignoring unsupported polishing-machine event {}", event == null ? null : event.eventType());
            return;
        }

        String itemIdentifier = resolveWaitingMessageBusinessKeyPort.resolve(ITEM_ARRIVED_AT_POLISHING_MACHINE_OUTPUT_MESSAGE);
        if (itemIdentifier == null) {
            return;
        }

        correlateMessagePort.correlateMessage(
                ITEM_ARRIVED_AT_POLISHING_MACHINE_OUTPUT_MESSAGE,
                itemIdentifier,
                Map.of("itemIdentifier", itemIdentifier));
    }
}
