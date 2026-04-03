package org.unisg.ftengrave.orderorchestrator.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;
import org.unisg.ftengrave.orderorchestrator.port.in.SendPerformQcCommandUseCase;
import org.unisg.ftengrave.orderorchestrator.port.out.SendPerformQcCommandPort;

@Service
@RequiredArgsConstructor
public class SendPerformQcCommandService implements SendPerformQcCommandUseCase {

    private final SendPerformQcCommandPort sendPerformQcCommandPort;

    @Override
    public void sendPerformQcCommand(String itemIdentifier, ItemColor targetColor) {
        sendPerformQcCommandPort.publish(itemIdentifier, targetColor);
    }
}
