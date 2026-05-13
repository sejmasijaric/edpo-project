package org.unisg.ftengrave.factoryeventstreams.dto;

public class SortingMachineEventDto {

  private String eventType;
  private String color;
  private String itemIdentifier;

  public SortingMachineEventDto() {
  }

  public SortingMachineEventDto(String eventType) {
    this.eventType = eventType;
  }

  public SortingMachineEventDto(String eventType, String color) {
    this.eventType = eventType;
    this.color = color;
  }

  public SortingMachineEventDto(String eventType, String color, String itemIdentifier) {
    this.eventType = eventType;
    this.color = color;
    this.itemIdentifier = itemIdentifier;
  }

  public String getEventType() {
    return eventType;
  }

  public void setEventType(String eventType) {
    this.eventType = eventType;
  }

  public String getColor() {
    return color;
  }

  public void setColor(String color) {
    this.color = color;
  }

  public String getItemIdentifier() {
    return itemIdentifier;
  }

  public void setItemIdentifier(String itemIdentifier) {
    this.itemIdentifier = itemIdentifier;
  }
}
