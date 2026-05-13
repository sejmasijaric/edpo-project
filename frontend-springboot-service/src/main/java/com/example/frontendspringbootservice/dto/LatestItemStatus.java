package com.example.frontendspringbootservice.dto;

public record LatestItemStatus(
        String itemIdentifier,
        String station,
        String outcomeType,
        String timestamp,
        String sourceTopic,
        long eventTimestampEpochMillis) {
}
