package com.example.frontendspringbootservice.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum OrderColor {
    RED("red"),
    WHITE("white"),
    BLUE("blue");

    private final String value;

    OrderColor(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static OrderColor fromValue(String value) {
        for (OrderColor color : values()) {
            if (color.value.equals(value)) {
                return color;
            }
        }
        throw new IllegalArgumentException("Invalid color: " + value);
    }
}
