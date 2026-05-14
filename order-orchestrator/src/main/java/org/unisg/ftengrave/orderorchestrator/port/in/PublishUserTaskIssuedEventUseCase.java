package org.unisg.ftengrave.orderorchestrator.port.in;

public interface PublishUserTaskIssuedEventUseCase {

    void publish(String itemIdentifier, String taskName, String targetColor);
}
