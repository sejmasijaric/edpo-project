package org.unisg.ftengrave.manufacturingservice.adapter.in.kafka.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class RunProductionCommandDto {

    @JsonAlias("eventType")
    private String commandType;
    private String itemIdentifier;

    public RunProductionCommandDto() {
    }

    public RunProductionCommandDto(String commandType, String itemIdentifier) {
        this.commandType = commandType;
        this.itemIdentifier = itemIdentifier;
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
}
