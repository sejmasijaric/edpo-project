package org.unisg.ftengrave.manufacturingservice.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.manufacturingservice.port.in.HandleWtMoveCompletedEventUseCase;
import org.unisg.ftengrave.manufacturingservice.port.in.WorkstationTransportEvent;
import org.unisg.ftengrave.manufacturingservice.port.out.CorrelateMessagePort;
import org.unisg.ftengrave.manufacturingservice.port.out.ResolveWaitingMessageBusinessKeyPort;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class WtMoveCompletedEventService implements HandleWtMoveCompletedEventUseCase {

    static final String WT_MOVE_COMPLETED_MESSAGE = "WtMoveCompletedMessage";
    private static final String WT_MOVE_COMPLETED_EVENT = "wt-move-completed";

    private final ResolveWaitingMessageBusinessKeyPort resolveWaitingMessageBusinessKeyPort;
    private final CorrelateMessagePort correlateMessagePort;

    @Override
    public void handle(WorkstationTransportEvent event) {
        if (!WT_MOVE_COMPLETED_EVENT.equals(event == null ? null : event.eventType())) {
            log.info("Ignoring unsupported workstation-transport event {}", event == null ? null : event.eventType());
            return;
        }

        String itemIdentifier = resolveWaitingMessageBusinessKeyPort.resolve(WT_MOVE_COMPLETED_MESSAGE);
        if (itemIdentifier == null) {
            return;
        }

        correlateMessagePort.correlateMessage(
                WT_MOVE_COMPLETED_MESSAGE,
                itemIdentifier,
                Map.of("itemIdentifier", itemIdentifier));
    }
}
