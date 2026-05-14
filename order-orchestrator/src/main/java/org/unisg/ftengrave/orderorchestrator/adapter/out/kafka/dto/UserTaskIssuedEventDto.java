package org.unisg.ftengrave.orderorchestrator.adapter.out.kafka.dto;

public record UserTaskIssuedEventDto(
        String commandType,
        String taskName,
        String taskCategory,
        String stationName,
        String targetColor) {
}
