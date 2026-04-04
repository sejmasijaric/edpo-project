package org.unisg.ftengrave.intakeservice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.intakeservice.domain.ItemColor;
import org.unisg.ftengrave.intakeservice.port.in.StartIntakeUseCase;
import org.unisg.ftengrave.intakeservice.port.out.CorrelateMessagePort;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class StartIntakeService implements StartIntakeUseCase {

    static final String START_INTAKE_MESSAGE = "StartIntakeMessage";

    private final CorrelateMessagePort correlateMessagePort;

    @Override
    public boolean startIntake(String itemIdentifier, ItemColor targetColor) {
        return correlateMessagePort.correlateMessage(
                START_INTAKE_MESSAGE,
                itemIdentifier,
                Map.of(
                        "itemIdentifier", itemIdentifier,
                        "targetColor", targetColor,
                        "item-arrived-at-intake", false,
                        "item-left-intake", false,
                        "item-arrived-at-engraver", false))
                != null;
    }
}
