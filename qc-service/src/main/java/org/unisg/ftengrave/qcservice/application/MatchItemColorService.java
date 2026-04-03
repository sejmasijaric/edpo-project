package org.unisg.ftengrave.qcservice.application;

import org.springframework.stereotype.Service;
import org.unisg.ftengrave.qcservice.domain.ItemColor;
import org.unisg.ftengrave.qcservice.domain.ItemColorMatch;

@Service
public class MatchItemColorService {

    public boolean matches(ItemColor detectedColor, ItemColor targetColor) {
        return new ItemColorMatch(detectedColor, targetColor).passed();
    }
}
