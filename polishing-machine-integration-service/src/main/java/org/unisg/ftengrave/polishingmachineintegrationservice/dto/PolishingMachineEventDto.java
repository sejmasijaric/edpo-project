package org.unisg.ftengrave.polishingmachineintegrationservice.dto;

public class PolishingMachineEventDto {

  private String eventType;

  public PolishingMachineEventDto() {
  }

  public PolishingMachineEventDto(String eventType) {
    this.eventType = eventType;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }
}
