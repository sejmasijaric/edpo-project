package org.unisg.ftengrave.intakeservice.port.out;

public interface MoveItemFromInputToEngraverPort {

    void publish(String itemIdentifier);
}
