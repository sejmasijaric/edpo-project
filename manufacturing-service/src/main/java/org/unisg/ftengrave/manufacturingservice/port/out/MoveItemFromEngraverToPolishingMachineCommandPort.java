package org.unisg.ftengrave.manufacturingservice.port.out;

public interface MoveItemFromEngraverToPolishingMachineCommandPort {

    void publish(String itemIdentifier);
}
