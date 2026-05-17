package com.example.frontendspringbootservice.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record UserTaskEvent(
        String itemIdentifier,
        String commandType,
        String taskName,
        String taskCategory,
        String stationName,
        String targetColor,
        String taskStatus,
        String errorMessage,
        Long eventTimestampEpochMillis) {

    public boolean isError() {
        return "error".equalsIgnoreCase(taskCategory) || errorMessage != null;
    }
}
