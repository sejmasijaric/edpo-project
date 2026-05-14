package org.unisg.ftengrave.dashboardservice.dto;

import java.util.Map;

public record DashboardEvent(
    String eventId,
    String itemIdentifier,
    String eventType,
    String sourceTopic,
    long timestamp,
    String taskName,
    String taskStatus,
    String stage,
    Long durationMillis,
    Map<String, String> attributes) {
}
