package org.unisg.ftengrave.intakeservice.port.in;

public interface PublishInsertItemIntoIntakeCommandUseCase {

    void publish(String itemIdentifier, String itemColor);
}
