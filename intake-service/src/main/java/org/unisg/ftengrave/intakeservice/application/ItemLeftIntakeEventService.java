package org.unisg.ftengrave.intakeservice.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.intakeservice.port.in.HandleItemLeftIntakeEventUseCase;
import org.unisg.ftengrave.intakeservice.port.in.VacuumGripperEvent;
import org.unisg.ftengrave.intakeservice.port.out.CorrelateMessagePort;
import org.unisg.ftengrave.intakeservice.port.out.ResolveWaitingMessageBusinessKeyPort;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemLeftIntakeEventService implements HandleItemLeftIntakeEventUseCase {

    static final String ITEM_LEFT_INTAKE_MESSAGE = "ItemLeftIntakeMessage";
    private static final String ITEM_LEFT_INTAKE_EVENT = "item-left-intake";

    private final ResolveWaitingMessageBusinessKeyPort resolveWaitingMessageBusinessKeyPort;
    private final CorrelateMessagePort correlateMessagePort;

    @Override
    public void handle(VacuumGripperEvent event) {
        if (!ITEM_LEFT_INTAKE_EVENT.equals(event == null ? null : event.eventType())) {
            log.info("Ignoring unsupported vacuum-gripper event {}", event == null ? null : event.eventType());
            return;
        }

        String itemIdentifier = resolveWaitingMessageBusinessKeyPort.resolve(ITEM_LEFT_INTAKE_MESSAGE);
        if (itemIdentifier == null) {
            return;
        }

        correlateMessagePort.correlateMessage(
                ITEM_LEFT_INTAKE_MESSAGE,
                itemIdentifier,
                Map.of(
                        "itemIdentifier", itemIdentifier,
                        "itemLeftIntake", true));
    }
}
