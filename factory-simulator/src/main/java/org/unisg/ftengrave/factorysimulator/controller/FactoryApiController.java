package org.unisg.ftengrave.factorysimulator.controller;

import java.util.List;
import org.unisg.ftengrave.factorysimulator.model.ManagedItem;
import org.unisg.ftengrave.factorysimulator.model.Sink;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulatorService;
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

  public FactoryApiController(FactorySimulatorService factorySimulatorService) {
    this.factorySimulatorService = factorySimulatorService;
  }

  @GetMapping("/sinks")
  public List<Sink> getSinks() {
    return factorySimulatorService.getSinks();
  }

  @GetMapping("/items")
  public List<ManagedItem> getItems() {
    return factorySimulatorService.getItems();
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
