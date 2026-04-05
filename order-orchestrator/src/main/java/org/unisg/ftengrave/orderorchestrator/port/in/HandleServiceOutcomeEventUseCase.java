package org.unisg.ftengrave.orderorchestrator.port.in;

public interface HandleServiceOutcomeEventUseCase {

    void handle(String itemIdentifier, String outcomeType);
}
