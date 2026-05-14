package org.unisg.ftengrave.dashboardservice.dto;

public record ManufacturingAttemptDuration(
    String itemIdentifier,
    String outcomeType,
    long startTimestamp,
    long endTimestamp,
    long durationMillis) {
}
