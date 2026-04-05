package org.unisg.ftengrave.manufacturingservice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.manufacturingservice.port.in.SendMoveItemFromEngraverToPolishingMachineCommandUseCase;
import org.unisg.ftengrave.manufacturingservice.port.out.MoveItemFromEngraverToPolishingMachineCommandPort;

@Service
@RequiredArgsConstructor
public class SendMoveItemFromEngraverToPolishingMachineCommandService
        implements SendMoveItemFromEngraverToPolishingMachineCommandUseCase {

    private final MoveItemFromEngraverToPolishingMachineCommandPort moveItemFromEngraverToPolishingMachineCommandPort;

    @Override
    public void send(String itemIdentifier) {
        moveItemFromEngraverToPolishingMachineCommandPort.publish(itemIdentifier);
    }
}
