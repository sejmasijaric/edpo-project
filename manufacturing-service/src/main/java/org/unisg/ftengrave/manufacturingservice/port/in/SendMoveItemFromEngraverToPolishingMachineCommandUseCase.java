package org.unisg.ftengrave.manufacturingservice.port.in;

public interface SendMoveItemFromEngraverToPolishingMachineCommandUseCase {

    void send(String itemIdentifier);
}
