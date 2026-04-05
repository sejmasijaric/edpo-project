package org.unisg.ftengrave.intakeservice.adapter.out.kafka.dto;

import org.unisg.ftengrave.intakeservice.domain.ItemColor;

public record InsertItemIntoIntakeCommandDto(String commandType, String stationName, ItemColor itemColor) {
}
