package org.unisg.ftengrave.qcservice.port.in;

public interface HandleItemArrivedAtQcEventUseCase {

    void handle(SortingMachineEvent event);
}
