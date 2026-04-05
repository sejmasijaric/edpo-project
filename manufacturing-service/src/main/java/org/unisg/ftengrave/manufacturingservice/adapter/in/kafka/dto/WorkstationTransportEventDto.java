package org.unisg.ftengrave.manufacturingservice.adapter.in.kafka.dto;

public class WorkstationTransportEventDto {

    private String eventType;

    public WorkstationTransportEventDto() {
    }

    public WorkstationTransportEventDto(String eventType) {
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
