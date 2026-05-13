package org.unisg.ftengrave.factoryeventstreams.dto;

public class MachineEventDto {

  private String eventType;
  private String itemIdentifier;

  public MachineEventDto() {
  }

  public MachineEventDto(String eventType) {
    this.eventType = eventType;
  }

  public MachineEventDto(String eventType, String itemIdentifier) {
    this.eventType = eventType;
    this.itemIdentifier = itemIdentifier;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getItemIdentifier() {
    return itemIdentifier;
  }

  public void setItemIdentifier(String itemIdentifier) {
    this.itemIdentifier = itemIdentifier;
  }
}
