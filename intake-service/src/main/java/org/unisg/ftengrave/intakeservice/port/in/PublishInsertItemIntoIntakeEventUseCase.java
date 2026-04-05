package org.unisg.ftengrave.intakeservice.port.in;

public interface PublishInsertItemIntoIntakeEventUseCase {

    void publish(String itemIdentifier, String itemColor);
}
