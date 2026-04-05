package org.unisg.ftengrave.orderorchestrator.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;
import org.unisg.ftengrave.orderorchestrator.port.in.StartOrderOrchestrationUseCase;
import org.unisg.ftengrave.orderorchestrator.port.out.CorrelateMessagePort;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class StartOrderOrchestrationService implements StartOrderOrchestrationUseCase {

    static final String ORDER_CREATED_MESSAGE = "OrderCreatedMessage";

    private final CorrelateMessagePort correlateMessagePort;

    @Override
    public boolean startOrderOrchestration(String itemIdentifier, ItemColor targetColor) {
        return correlateMessagePort.correlateMessage(
                ORDER_CREATED_MESSAGE,
                itemIdentifier,
                Map.of(
                        "itemIdentifier", itemIdentifier,
                        "targetColor", targetColor))
                != null;
    }
}
