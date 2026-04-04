package org.unisg.ftengrave.intakeservice.port.in;

public interface HandleItemLeftIntakeEventUseCase {

    void handle(VacuumGripperEvent event);
}
