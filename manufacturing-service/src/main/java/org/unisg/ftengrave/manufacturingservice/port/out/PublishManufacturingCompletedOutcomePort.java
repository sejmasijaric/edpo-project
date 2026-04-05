package org.unisg.ftengrave.manufacturingservice.port.out;

public interface PublishManufacturingCompletedOutcomePort {

    void publish(String itemIdentifier);
}
