package org.unisg.ftengrave.qcservice.port.in;

import org.unisg.ftengrave.qcservice.domain.ItemColor;

public interface MatchItemColorUseCase {

    boolean matches(ItemColor detectedColor, ItemColor targetColor);
}
