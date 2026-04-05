package org.unisg.ftengrave.orderorchestrator.port.in;

import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;

public interface SendRunIntakeCommandUseCase {

    void sendRunIntakeCommand(String itemIdentifier, ItemColor targetColor);
}
