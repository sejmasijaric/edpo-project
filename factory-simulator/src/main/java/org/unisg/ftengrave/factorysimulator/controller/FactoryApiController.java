package org.unisg.ftengrave.factorysimulator.controller;

import java.util.List;
import org.unisg.ftengrave.factorysimulator.domain.ItemColor;
import org.unisg.ftengrave.factorysimulator.domain.MachineStatus;
import org.unisg.ftengrave.factorysimulator.domain.ManagedItem;
import org.unisg.ftengrave.factorysimulator.domain.Sink;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulatorService;
import org.unisg.ftengrave.factorysimulator.service.OneWayPointToPointTransportService;
import org.unisg.ftengrave.factorysimulator.service.VacuumGripperService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class FactoryApiController {

  private final FactorySimulatorService factorySimulatorService;
  private final VacuumGripperService vgrService;
  private final VacuumGripperService wtService;
  private final OneWayPointToPointTransportService sorterService;

  public FactoryApiController(
      FactorySimulatorService factorySimulatorService,
      @Qualifier("vgrService") VacuumGripperService vgrService,
      @Qualifier("wtService") VacuumGripperService wtService,
      @Qualifier("sorterService") OneWayPointToPointTransportService sorterService) {
    this.factorySimulatorService = factorySimulatorService;
    this.vgrService = vgrService;
    this.wtService = wtService;
    this.sorterService = sorterService;
  }

  @GetMapping("/sinks")
  public List<Sink> getSinks() {
    return factorySimulatorService.getSinks();
  }

  @GetMapping("/items")
  public List<ManagedItem> getItems() {
    return factorySimulatorService.getItems();
  }

  @GetMapping("/vgr/status")
  public MachineStatus getVacuumGripperStatus() {
    return vgrService.getStatus();
  }

  @GetMapping("/wt/status")
  public MachineStatus getWorkstationTransportStatus() {
    return wtService.getStatus();
  }

  @GetMapping("/sm/status")
  public MachineStatus getSorterStatus() {
    return sorterService.getStatus();
  }

  @PostMapping("/items")
  @ResponseStatus(HttpStatus.CREATED)
  public void addItem(
      @RequestParam String itemId,
      @RequestParam ItemColor color,
      @RequestParam String sinkId) {
    factorySimulatorService.addItem(itemId, color, sinkId);
  }

  @PostMapping("/items/{itemId}/move")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void moveItem(@PathVariable String itemId, @RequestParam String targetSinkId) {
    factorySimulatorService.moveItem(itemId, targetSinkId);
  }

  @DeleteMapping("/items/{itemId}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void deleteItem(@PathVariable String itemId) {
    factorySimulatorService.deleteItem(itemId);
  }

  @ExceptionHandler({IllegalStateException.class, IllegalArgumentException.class})
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public String handleBadRequest(RuntimeException exception) {
    return exception.getMessage();
  }

  @ExceptionHandler(java.util.NoSuchElementException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  public String handleNotFound(RuntimeException exception) {
    return exception.getMessage();
  }
}
