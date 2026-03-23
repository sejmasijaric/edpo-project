package org.unisg.ftengrave.factorysimulator.controller;

import java.util.NoSuchElementException;
import org.unisg.ftengrave.factorysimulator.service.VacuumGripperService;
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

  private final VacuumGripperService vacuumGripperService;

  public VacuumGripperController(VacuumGripperService vacuumGripperService) {
    this.vacuumGripperService = vacuumGripperService;
  }

  @GetMapping("/pick_up_and_transport")
  public VacuumGripperResponse pickUpAndTransport(
      @RequestParam String machine,
      @RequestParam String start,
      @RequestParam String end) {
    return vacuumGripperService.pickUpAndTransport(machine, start, end);
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
}
