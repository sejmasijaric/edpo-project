package org.unisg.ftengrave.factorysimulator.controller;

import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import org.unisg.ftengrave.factorysimulator.service.VacuumGripperService;
import org.unisg.ftengrave.factorysimulator.service.VacuumGripperService.VacuumGripperExecution;
import org.unisg.ftengrave.factorysimulator.service.VacuumGripperService.VacuumGripperResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/vgr")
public class VacuumGripperController {

  private static final DateTimeFormatter FACTORY_TIMESTAMP_FORMAT =
      DateTimeFormatter.ofPattern("dd-MMM-uuuu (HH:mm:ss.SSSSSS)", Locale.ENGLISH);

  private final VacuumGripperService vacuumGripperService;

  public VacuumGripperController(VacuumGripperService vacuumGripperService) {
    this.vacuumGripperService = vacuumGripperService;
  }

  @GetMapping("/pick_up_and_transport")
  public VacuumGripperResponse pickUpAndTransport(
      HttpServletRequest request,
      @RequestParam String machine,
      @RequestParam String start,
      @RequestParam String end) {
    VacuumGripperExecution execution = vacuumGripperService.pickUpAndTransport(machine, start, end);
    return new VacuumGripperResponse(
        List.of(),
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
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String handleNotFound(RuntimeException exception) {
    return exception.getMessage();
  }

  private String formatTimestamp(LocalDateTime timestamp) {
    return FACTORY_TIMESTAMP_FORMAT.format(timestamp);
  }

  private String formatProcessTime(Duration duration) {
    long totalSeconds = duration.toSeconds();
    long hours = totalSeconds / 3600;
    long minutes = (totalSeconds % 3600) / 60;
    long seconds = totalSeconds % 60;
    long micros = duration.toNanosPart() / 1_000;
    return "%d:%02d:%02d.%06d".formatted(hours, minutes, seconds, micros);
  }
}
