package org.unisg.ftengrave.orderorchestrator.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;
import org.unisg.ftengrave.orderorchestrator.port.in.SendRunIntakeCommandUseCase;
import org.unisg.ftengrave.orderorchestrator.port.out.SendRunIntakeCommandPort;

@Service
@RequiredArgsConstructor
public class SendRunIntakeCommandService implements SendRunIntakeCommandUseCase {

    private final SendRunIntakeCommandPort sendRunIntakeCommandPort;

    @Override
    public void sendRunIntakeCommand(String itemIdentifier, ItemColor targetColor) {
        sendRunIntakeCommandPort.publish(itemIdentifier, targetColor);
    }
}
