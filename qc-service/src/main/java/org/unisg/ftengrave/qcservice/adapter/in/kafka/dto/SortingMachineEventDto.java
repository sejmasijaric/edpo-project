package org.unisg.ftengrave.qcservice.adapter.in.kafka.dto;

public class SortingMachineEventDto {

    private String eventType;
    private String color;

    public SortingMachineEventDto() {
    }

    public SortingMachineEventDto(String eventType) {
        this.eventType = eventType;
    }

    public SortingMachineEventDto(String eventType, String color) {
        this.eventType = eventType;
        this.color = color;
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
}
