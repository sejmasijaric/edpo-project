package org.unisg.ftengrave.orderorchestrator.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.orderorchestrator.port.in.StartOrderOrchestrationUseCase;
import org.unisg.ftengrave.orderorchestrator.port.out.CorrelateMessagePort;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class StartOrderOrchestrationService implements StartOrderOrchestrationUseCase {

    static final String START_ORDER_ORCHESTRATION_MESSAGE = "StartOrderOrchestrationMessage";

    private final CorrelateMessagePort correlateMessagePort;

    @Override
    public boolean startOrderOrchestration(String orderIdentifier) {
        return correlateMessagePort.correlateMessage(
                START_ORDER_ORCHESTRATION_MESSAGE,
                orderIdentifier,
                Map.of("orderIdentifier", orderIdentifier))
                != null;
    }
}
