package org.unisg.ftengrave.factorysimulator.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;
import org.unisg.ftengrave.factorysimulator.domain.Item;

public class OneWayPointToPointTransportService {

  private static final Duration SOURCE_POLL_INTERVAL = Duration.ofMillis(50);

  private final FactorySimulatorService factorySimulatorService;
  private final Duration movementDelay;
  private final OneWayTransportMachine machine;

  public OneWayPointToPointTransportService(
      FactorySimulatorService factorySimulatorService,
      FactorySimulationProperties properties,
      String machineName,
      String acceptedStart,
      String inputSink,
      List<String> previousSinks,
      String holdSink) {
    this.factorySimulatorService = factorySimulatorService;
    this.movementDelay = properties.getMovementDelay();
    this.machine = new OneWayTransportMachine(
        machineName,
        acceptedStart,
        inputSink,
        List.copyOf(previousSinks),
        holdSink);
  }

  public OneWayTransportExecution transport(
      String machine,
      String start,
      Function<Item, String> targetSinkResolver) {
    validate(machine, start);

    LocalDateTime startTime = LocalDateTime.now();
    waitForInputItem();

    factorySimulatorService.moveItemBetweenSinks(
        this.machine.inputSink(), this.machine.holdSink(), false);
    advancePreviousSinkToInput();

    waitBetweenMovements();

    Item itemInHold = factorySimulatorService.getSink(this.machine.holdSink()).item();
    if (itemInHold != null) {
      String targetSinkId = targetSinkResolver.apply(itemInHold);
      factorySimulatorService.tryMoveItemBetweenSinks(
          this.machine.holdSink(), targetSinkId, true);
    }

    LocalDateTime endTime = LocalDateTime.now();
    return new OneWayTransportExecution(startTime, endTime, Duration.between(startTime, endTime));
  }

  private void validate(String machine, String start) {
    if (!this.machine.name().equals(machine)) {
      throw new IllegalArgumentException("Unsupported one way transport machine: " + machine);
    }
    if (!this.machine.acceptedStart().equals(start)) {
      throw new IllegalArgumentException("Unknown start for machine " + machine + ": " + start);
    }
  }

  private void waitForInputItem() {
    while (true) {
      if (factorySimulatorService.getSink(this.machine.inputSink()).item() != null) {
        return;
      }

      if (advancePreviousSinkToInput()) {
        return;
      }

      waitForSourceItem();
    }
  }

  private boolean advancePreviousSinkToInput() {
    for (String previousSink : this.machine.previousSinks()) {
      if (factorySimulatorService.tryMoveItemBetweenSinks(
          previousSink, this.machine.inputSink(), true)) {
        if (factorySimulatorService.getSink(this.machine.inputSink()).item() != null) {
          return true;
        }
      }
    }
    return false;
  }

  private void waitBetweenMovements() {
    try {
      Thread.sleep(movementDelay.toMillis());
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("One way transport motion was interrupted", exception);
    }
  }

  private void waitForSourceItem() {
    try {
      Thread.sleep(SOURCE_POLL_INTERVAL.toMillis());
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Waiting for source item was interrupted", exception);
    }
  }

  private record OneWayTransportMachine(
      String name,
      String acceptedStart,
      String inputSink,
      List<String> previousSinks,
      String holdSink) {
  }

  public record OneWayTransportExecution(
      LocalDateTime startTime,
      LocalDateTime endTime,
      Duration processTime) {
  }

  public record OneWayTransportResponse(
      List<?> attributes,
      String end_time,
      String link,
      String process_time,
      String start_time) {
  }
}
