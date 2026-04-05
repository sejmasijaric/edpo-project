package org.unisg.ftengrave.manufacturingservice.port.out;

public interface PublishManufacturingFailedOutcomePort {

    void publish(String itemIdentifier);
}
