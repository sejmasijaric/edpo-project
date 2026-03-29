package org.unisg.ftengrave.sorterintegrationservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class SortingMachineCommandDto {

  @JsonAlias("eventType")
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
