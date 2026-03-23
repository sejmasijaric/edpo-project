package org.unisg.ftengrave.factorysimulator.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import org.unisg.ftengrave.factorysimulator.service.OneWayPointToPointTransportService;
import org.unisg.ftengrave.factorysimulator.service.OneWayPointToPointTransportService.OneWayTransportExecution;
import org.unisg.ftengrave.factorysimulator.service.OneWayPointToPointTransportService.OneWayTransportResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/sm")
public class SorterController {

  private static final DateTimeFormatter FACTORY_TIMESTAMP_FORMAT =
      DateTimeFormatter.ofPattern("dd-MMM-uuuu (HH:mm:ss.SSSSSS)", Locale.ENGLISH);
  private static final Map<String, String> EJECTION_SINK_MAPPING = Map.of(
      "sink_1", "SINK-S1",
      "sink_2", "SINK-S2",
      "sink_3", "SINK-S3");

  private final OneWayPointToPointTransportService sorterService;

  public SorterController(
      @Qualifier("sorterService") OneWayPointToPointTransportService sorterService) {
    this.sorterService = sorterService;
  }

  @GetMapping("/sort")
  public OneWayTransportResponse sort(
      HttpServletRequest request,
      @RequestParam String machine,
      @RequestParam String start,
      @RequestParam(name = "predefined_ejection_location") String predefinedEjectionLocation) {
    String targetSink = mapEjectionLocation(predefinedEjectionLocation);

    OneWayTransportExecution execution =
        sorterService.transport(machine, start, item -> targetSink);

    return new OneWayTransportResponse(
        List.of(new SorterAttribute(null)),
        formatTimestamp(execution.endTime()),
        request.getRequestURL().toString(),
        formatProcessTime(execution.processTime()),
        formatTimestamp(execution.startTime()));
  }

  @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String handleBadRequest(RuntimeException exception) {
    return exception.getMessage();
  }

  @ExceptionHandler(NoSuchElementException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String handleNotFound(RuntimeException exception) {
    return exception.getMessage();
  }

  private String formatTimestamp(LocalDateTime timestamp) {
    return FACTORY_TIMESTAMP_FORMAT.format(timestamp);
  }

  private String mapEjectionLocation(String predefinedEjectionLocation) {
    String sinkId = EJECTION_SINK_MAPPING.get(predefinedEjectionLocation);
    if (sinkId == null) {
      throw new IllegalArgumentException(
          "Unknown predefined ejection location: " + predefinedEjectionLocation);
    }
    return sinkId;
  }

  private String formatProcessTime(Duration duration) {
    long totalSeconds = duration.toSeconds();
    long hours = totalSeconds / 3600;
    long minutes = (totalSeconds % 3600) / 60;
    long seconds = totalSeconds % 60;
    long micros = duration.toNanosPart() / 1_000;
    return "%d:%02d:%02d.%06d".formatted(hours, minutes, seconds, micros);
  }

  private record SorterAttribute(String sink) {
  }
}
