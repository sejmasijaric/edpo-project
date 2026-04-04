package org.unisg.ftengrave.intakeservice.adapter.in.kafka.dto;

public class EngraverEventDto {

    private String eventType;

    public EngraverEventDto() {
    }

    public EngraverEventDto(String eventType) {
        this.eventType = eventType;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
}
