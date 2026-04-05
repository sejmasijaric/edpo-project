package org.unisg.ftengrave.orderorchestrator.port.out;

import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;

public interface SendRunIntakeCommandPort {

    void publish(String itemIdentifier, ItemColor targetColor);
}
