package org.unisg.ftengrave.dashboardservice.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

public record DashboardMetricsResponse(
    Instant from,
    Instant to,
    QcRejectedRate qcRejectedRate,
    AverageDuration averageManufacturingTime,
    ManualInterventions manualInterventions,
    ManufacturingFailureRate manufacturingFailureRate,
    DurationStats averageEndToEndProductionTime,
    Map<String, Long> workInProgressByStage,
    RetryRate retryRate) {

  public record QcRejectedRate(long rejectedCount, long passedCount, long totalCount, double rejectedPercentage) {
  }

  public record AverageDuration(long completedAttemptCount, double averageDurationMillis) {
  }

  public record ManualInterventions(long openCount, long completedCount, List<OpenIntervention> openInterventions) {
  }

  public record OpenIntervention(String itemIdentifier, String taskName, String currentStage) {
  }

  public record ManufacturingFailureRate(
      long failedCount,
      long completedCount,
      long totalCount,
      double failurePercentage,
      long failedItemCount) {
  }

  public record DurationStats(
      long completedCount,
      double averageDurationMillis,
      long minimumDurationMillis,
      long maximumDurationMillis) {
  }

  public record RetryRate(
      long completedItemCount,
      long totalRetries,
      double averageRetriesPerCompletedItem,
      Map<String, Long> retriesPerItem) {
  }
}
