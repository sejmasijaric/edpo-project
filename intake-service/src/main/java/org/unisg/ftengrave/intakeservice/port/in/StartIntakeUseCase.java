package org.unisg.ftengrave.intakeservice.port.in;

import org.unisg.ftengrave.intakeservice.domain.ItemColor;

public interface StartIntakeUseCase {

    boolean startIntake(String itemIdentifier, ItemColor targetColor);
}
