package org.unisg.ftengrave.orderorchestrator.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.orderorchestrator.port.in.SendRunProductionCommandUseCase;
import org.unisg.ftengrave.orderorchestrator.port.out.SendRunProductionCommandPort;

@Service
@RequiredArgsConstructor
public class SendRunProductionCommandService implements SendRunProductionCommandUseCase {

    private final SendRunProductionCommandPort sendRunProductionCommandPort;

    @Override
    public void sendRunProductionCommand(String itemIdentifier) {
        sendRunProductionCommandPort.publish(itemIdentifier);
    }
}
