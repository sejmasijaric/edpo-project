package org.unisg.ftengrave.intakeservice.adapter.out.kafka.dto;

import org.unisg.ftengrave.intakeservice.domain.ItemColor;

public record InsertItemIntoIntakeEventDto(String eventType, String stationName, ItemColor itemColor) {
}
