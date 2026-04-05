package org.unisg.ftengrave.intakeservice.adapter.in.kafka.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.unisg.ftengrave.intakeservice.domain.ItemColor;

public class RunItemIntakeCommandDto {

    @JsonAlias("eventType")
    private String commandType;
    private String itemIdentifier;
    private ItemColor targetColor;

    public RunItemIntakeCommandDto() {
    }

    public RunItemIntakeCommandDto(String commandType, String itemIdentifier, ItemColor targetColor) {
        this.commandType = commandType;
        this.itemIdentifier = itemIdentifier;
        this.targetColor = targetColor;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }

    public String getItemIdentifier() {
        return itemIdentifier;
    }

    public void setItemIdentifier(String itemIdentifier) {
        this.itemIdentifier = itemIdentifier;
    }

    public ItemColor getTargetColor() {
        return targetColor;
    }

    public void setTargetColor(ItemColor targetColor) {
        this.targetColor = targetColor;
    }
}
