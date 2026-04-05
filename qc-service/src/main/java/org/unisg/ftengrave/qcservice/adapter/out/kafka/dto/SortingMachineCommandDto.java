package org.unisg.ftengrave.qcservice.adapter.out.kafka.dto;

public class SortingMachineCommandDto {

    private String commandType;

    public SortingMachineCommandDto() {
    }

    public SortingMachineCommandDto(String commandType) {
        this.commandType = commandType;
    }

    public String getCommandType() {
        return commandType;
    }

    public void setCommandType(String commandType) {
        this.commandType = commandType;
    }
}
