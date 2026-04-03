package org.unisg.ftengrave.orderorchestrator.port.in;

public interface HandleQcOutcomeEventUseCase {

    void handle(String itemIdentifier, String outcomeType);
}
