package org.unisg.ftengrave.intakeservice.port.in;

public interface PublishUserTaskIssuedEventUseCase {

    void publish(String itemIdentifier, String taskName, String targetColor);
}
