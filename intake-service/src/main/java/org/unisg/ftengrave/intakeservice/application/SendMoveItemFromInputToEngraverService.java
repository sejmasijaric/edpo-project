package org.unisg.ftengrave.intakeservice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.intakeservice.port.in.SendMoveItemFromInputToEngraverUseCase;
import org.unisg.ftengrave.intakeservice.port.out.MoveItemFromInputToEngraverPort;

@Service
@RequiredArgsConstructor
public class SendMoveItemFromInputToEngraverService implements SendMoveItemFromInputToEngraverUseCase {

    private final MoveItemFromInputToEngraverPort moveItemFromInputToEngraverPort;

    @Override
    public void send(String itemIdentifier) {
        moveItemFromInputToEngraverPort.publish(itemIdentifier);
    }
}
