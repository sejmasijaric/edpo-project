package org.unisg.ftengrave.vacuumgripperintegrationservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class VacuumGripperCommandDto {

  @JsonAlias("eventType")
  private String commandType;

  public VacuumGripperCommandDto() {
  }

  public VacuumGripperCommandDto(String commandType) {
    this.commandType = commandType;
  }

  public String getCommandType() {
    return commandType;
  }

  public void setCommandType(String commandType) {
    this.commandType = commandType;
  }
}
