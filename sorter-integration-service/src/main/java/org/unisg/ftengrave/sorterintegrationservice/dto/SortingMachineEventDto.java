package org.unisg.ftengrave.sorterintegrationservice.dto;

public class SortingMachineEventDto {

  private String eventType;

  public SortingMachineEventDto() {
  }

  public SortingMachineEventDto(String eventType) {
    this.eventType = eventType;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }
}
