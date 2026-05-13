package org.unisg.ftengrave.factoryeventstreams.dto;

public record LatestItemStatus(
    String itemIdentifier,
    String station,
    String outcomeType,
    String timestamp,
    String sourceTopic,
    long eventTimestampEpochMillis) {

  public boolean isAfterOrSameAs(LatestItemStatus other) {
    return other == null || eventTimestampEpochMillis >= other.eventTimestampEpochMillis();
  }
}
