package org.unisg.ftengrave.workstationtransportintegrationservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class WorkstationTransportCommandDto {

  @JsonAlias("eventType")
  private String commandType;

  public WorkstationTransportCommandDto() {
  }

  public WorkstationTransportCommandDto(String commandType) {
    this.commandType = commandType;
  }

  public String getCommandType() {
    return commandType;
  }

  public void setCommandType(String commandType) {
    this.commandType = commandType;
  }
}
