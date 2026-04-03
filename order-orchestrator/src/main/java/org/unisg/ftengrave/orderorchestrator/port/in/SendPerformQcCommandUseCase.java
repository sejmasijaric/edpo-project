package org.unisg.ftengrave.orderorchestrator.port.in;

import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;

public interface SendPerformQcCommandUseCase {

    void sendPerformQcCommand(String itemIdentifier, ItemColor targetColor);
}
