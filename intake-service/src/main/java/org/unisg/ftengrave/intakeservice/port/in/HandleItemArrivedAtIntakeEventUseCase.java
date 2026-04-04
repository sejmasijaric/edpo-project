package org.unisg.ftengrave.intakeservice.port.in;

public interface HandleItemArrivedAtIntakeEventUseCase {

    void handle(VacuumGripperEvent event);
}
