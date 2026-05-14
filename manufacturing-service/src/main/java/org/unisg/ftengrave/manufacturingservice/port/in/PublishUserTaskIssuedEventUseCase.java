package org.unisg.ftengrave.manufacturingservice.port.in;

public interface PublishUserTaskIssuedEventUseCase {

    void publish(String itemIdentifier, String taskName);
}
