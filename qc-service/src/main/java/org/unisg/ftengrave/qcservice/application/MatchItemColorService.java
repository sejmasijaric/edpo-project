package org.unisg.ftengrave.qcservice.application;

import org.springframework.stereotype.Service;
import org.unisg.ftengrave.qcservice.domain.ItemColor;
import org.unisg.ftengrave.qcservice.domain.ItemColorMatch;
import org.unisg.ftengrave.qcservice.port.in.MatchItemColorUseCase;

@Service
public class MatchItemColorService implements MatchItemColorUseCase {

    @Override
    public boolean matches(ItemColor detectedColor, ItemColor targetColor) {
        return new ItemColorMatch(detectedColor, targetColor).passed();
    }
}
