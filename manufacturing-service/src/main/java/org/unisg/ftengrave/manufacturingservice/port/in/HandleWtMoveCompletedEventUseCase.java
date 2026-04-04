package org.unisg.ftengrave.manufacturingservice.port.in;

public interface HandleWtMoveCompletedEventUseCase {

    void handle(WorkstationTransportEvent event);
}
