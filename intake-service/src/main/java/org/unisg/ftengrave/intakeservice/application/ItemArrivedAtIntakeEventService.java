package org.unisg.ftengrave.intakeservice.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.intakeservice.port.in.HandleItemArrivedAtIntakeEventUseCase;
import org.unisg.ftengrave.intakeservice.port.in.VacuumGripperEvent;
import org.unisg.ftengrave.intakeservice.port.out.CorrelateMessagePort;
import org.unisg.ftengrave.intakeservice.port.out.ResolveWaitingMessageBusinessKeyPort;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ItemArrivedAtIntakeEventService implements HandleItemArrivedAtIntakeEventUseCase {

    static final String ITEM_ARRIVED_AT_INTAKE_MESSAGE = "ItemArrivedAtIntakeMessage";
    private static final String ITEM_ARRIVED_AT_INTAKE_EVENT = "item-arrived-at-intake";

    private final ResolveWaitingMessageBusinessKeyPort resolveWaitingMessageBusinessKeyPort;
    private final CorrelateMessagePort correlateMessagePort;

    @Override
    public void handle(VacuumGripperEvent event) {
        if (!ITEM_ARRIVED_AT_INTAKE_EVENT.equals(event == null ? null : event.eventType())) {
            log.info("Ignoring unsupported vacuum-gripper event {}", event == null ? null : event.eventType());
            return;
        }

        String itemIdentifier = resolveWaitingMessageBusinessKeyPort.resolve(ITEM_ARRIVED_AT_INTAKE_MESSAGE);
        if (itemIdentifier == null) {
            return;
        }

        correlateMessagePort.correlateMessage(
                ITEM_ARRIVED_AT_INTAKE_MESSAGE,
                itemIdentifier,
                Map.of(
                        "itemIdentifier", itemIdentifier,
                        "itemArrivedAtIntake", true));
    }
}
