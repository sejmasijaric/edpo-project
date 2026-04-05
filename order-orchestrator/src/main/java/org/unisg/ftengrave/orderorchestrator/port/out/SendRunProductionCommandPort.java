package org.unisg.ftengrave.orderorchestrator.port.out;

public interface SendRunProductionCommandPort {

    void publish(String itemIdentifier);
}
