package org.unisg.ftengrave.engraverintegrationservice.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public class EngraverCommandDto {

  @JsonAlias("eventType")
  private String commandType;

  public EngraverCommandDto() {
  }

  public EngraverCommandDto(String commandType) {
    this.commandType = commandType;
  }

  public String getCommandType() {
    return commandType;
  }

  public void setCommandType(String commandType) {
    this.commandType = commandType;
  }
}
