package org.unisg.ftengrave.manufacturingservice.adapter.out.kafka.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class PolishingMachineCommandDto {

    @JsonAlias("eventType")
    private String commandType;

    public PolishingMachineCommandDto() {
    }

    public PolishingMachineCommandDto(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }
}
