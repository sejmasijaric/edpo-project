package org.unisg.ftengrave.qcservice.port.in;

public interface PublishUserTaskIssuedEventUseCase {

    void publish(String itemIdentifier, String taskName, String targetColor);
}
