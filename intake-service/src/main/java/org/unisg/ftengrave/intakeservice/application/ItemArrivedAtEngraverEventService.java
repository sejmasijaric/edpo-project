package org.unisg.ftengrave.intakeservice.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.intakeservice.port.in.EngraverEvent;
import org.unisg.ftengrave.intakeservice.port.in.HandleItemArrivedAtEngraverEventUseCase;
import org.unisg.ftengrave.intakeservice.port.out.CorrelateMessagePort;
import org.unisg.ftengrave.intakeservice.port.out.ResolveWaitingMessageBusinessKeyPort;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemArrivedAtEngraverEventService implements HandleItemArrivedAtEngraverEventUseCase {

    static final String ITEM_ARRIVED_AT_ENGRAVER_MESSAGE = "ItemArrivedAtEngraverMessage";
    private static final String ITEM_ARRIVED_AT_ENGRAVER_EVENT = "item-arrived-at-engraver-sink";

    private final ResolveWaitingMessageBusinessKeyPort resolveWaitingMessageBusinessKeyPort;
    private final CorrelateMessagePort correlateMessagePort;

    @Override
    public void handle(EngraverEvent event) {
        if (!ITEM_ARRIVED_AT_ENGRAVER_EVENT.equals(event == null ? null : event.eventType())) {
            log.info("Ignoring unsupported engraver event {}", event == null ? null : event.eventType());
            return;
        }

        String itemIdentifier = resolveWaitingMessageBusinessKeyPort.resolve(ITEM_ARRIVED_AT_ENGRAVER_MESSAGE);
        if (itemIdentifier == null) {
            return;
        }

        correlateMessagePort.correlateMessage(
                ITEM_ARRIVED_AT_ENGRAVER_MESSAGE,
                itemIdentifier,
                Map.of(
                        "itemIdentifier", itemIdentifier,
                        "itemArrivedAtEngraver", true));
    }
}
