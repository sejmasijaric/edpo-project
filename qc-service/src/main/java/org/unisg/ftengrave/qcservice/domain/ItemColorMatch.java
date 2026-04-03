package org.unisg.ftengrave.qcservice.domain;

import java.util.Objects;

public final class ItemColorMatch {

    private final ItemColor detectedColor;
    private final ItemColor targetColor;

    public ItemColorMatch(ItemColor detectedColor, ItemColor targetColor) {
        this.detectedColor = Objects.requireNonNull(detectedColor, "detectedColor must not be null");
        this.targetColor = Objects.requireNonNull(targetColor, "targetColor must not be null");
    }

    public boolean passed() {
        return detectedColor == targetColor;
    }
}
