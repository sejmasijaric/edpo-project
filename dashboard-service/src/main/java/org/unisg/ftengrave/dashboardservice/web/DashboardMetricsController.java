package org.unisg.ftengrave.dashboardservice.web;

import java.time.Instant;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.unisg.ftengrave.dashboardservice.dto.DashboardMetricsResponse;

@RestController
public class DashboardMetricsController {

  private final DashboardMetricsService dashboardMetricsService;

  public DashboardMetricsController(DashboardMetricsService dashboardMetricsService) {
    this.dashboardMetricsService = dashboardMetricsService;
  }

  @GetMapping("/api/dashboard/metrics")
  public DashboardMetricsResponse metrics(
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant from,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant to) {
    Instant effectiveTo = to == null ? Instant.now() : to;
    Instant effectiveFrom = from == null ? effectiveTo.minusSeconds(3600) : from;
    return dashboardMetricsService.metrics(effectiveFrom, effectiveTo);
  }
}
