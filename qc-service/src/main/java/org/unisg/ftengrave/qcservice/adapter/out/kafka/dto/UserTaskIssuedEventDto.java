package org.unisg.ftengrave.qcservice.adapter.out.kafka.dto;

public record UserTaskIssuedEventDto(
        String commandType,
        String taskName,
        String taskCategory,
        String stationName,
        String targetColor) {
}
