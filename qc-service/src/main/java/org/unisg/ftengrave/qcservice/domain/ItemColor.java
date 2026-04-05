package org.unisg.ftengrave.qcservice.domain;

public enum ItemColor {
    RED,
    WHITE,
    BLUE,
    NONE;

    public static ItemColor fromExternalValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return switch (value.trim().toLowerCase()) {
            case "white" -> WHITE;
            case "red" -> RED;
            case "blue" -> BLUE;
            default -> NONE;
        };
    }
}
