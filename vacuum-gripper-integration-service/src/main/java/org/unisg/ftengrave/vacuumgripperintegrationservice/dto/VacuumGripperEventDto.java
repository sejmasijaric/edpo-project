package org.unisg.ftengrave.vacuumgripperintegrationservice.dto;

public class VacuumGripperEventDto {

  private String eventType;

  public VacuumGripperEventDto() {
  }

  public VacuumGripperEventDto(String eventType) {
    this.eventType = eventType;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }
}
