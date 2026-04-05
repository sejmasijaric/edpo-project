package org.unisg.ftengrave.manufacturingservice.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.manufacturingservice.port.in.EngraverEvent;
import org.unisg.ftengrave.manufacturingservice.port.in.HandleItemArrivedAtEngraverSinkEventUseCase;
import org.unisg.ftengrave.manufacturingservice.port.out.CorrelateMessagePort;
import org.unisg.ftengrave.manufacturingservice.port.out.ResolveWaitingMessageBusinessKeyPort;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemArrivedAtEngraverSinkEventService implements HandleItemArrivedAtEngraverSinkEventUseCase {

    static final String ITEM_ARRIVED_AT_ENGRAVER_SINK_MESSAGE = "ItemArrivedEngraverSinkMessage";
    private static final String ITEM_ARRIVED_AT_ENGRAVER_SINK_EVENT = "item-arrived-at-engraver-sink";

    private final ResolveWaitingMessageBusinessKeyPort resolveWaitingMessageBusinessKeyPort;
    private final CorrelateMessagePort correlateMessagePort;

    @Override
    public void handle(EngraverEvent event) {
        if (!ITEM_ARRIVED_AT_ENGRAVER_SINK_EVENT.equals(event == null ? null : event.eventType())) {
            log.info("Ignoring unsupported engraver event {}", event == null ? null : event.eventType());
            return;
        }

        String itemIdentifier = resolveWaitingMessageBusinessKeyPort.resolve(ITEM_ARRIVED_AT_ENGRAVER_SINK_MESSAGE);
        if (itemIdentifier == null) {
            return;
        }

        correlateMessagePort.correlateMessage(
                ITEM_ARRIVED_AT_ENGRAVER_SINK_MESSAGE,
                itemIdentifier,
                Map.of("itemIdentifier", itemIdentifier));
    }
}
