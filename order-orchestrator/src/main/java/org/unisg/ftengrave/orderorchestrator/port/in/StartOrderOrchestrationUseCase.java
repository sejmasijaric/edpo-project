package org.unisg.ftengrave.orderorchestrator.port.in;

import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;

public interface StartOrderOrchestrationUseCase {

    boolean startOrderOrchestration(String itemIdentifier, ItemColor targetColor);
}
