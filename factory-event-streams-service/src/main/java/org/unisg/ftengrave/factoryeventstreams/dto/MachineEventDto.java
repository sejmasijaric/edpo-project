package org.unisg.ftengrave.factoryeventstreams.dto;

public class MachineEventDto {

  private String eventType;

  public MachineEventDto() {
  }

  public MachineEventDto(String eventType) {
    this.eventType = eventType;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }
}
