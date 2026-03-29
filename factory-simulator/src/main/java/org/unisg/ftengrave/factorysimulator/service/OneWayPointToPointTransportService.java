package org.unisg.ftengrave.factorysimulator.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import org.unisg.ftengrave.factorysimulator.domain.Item;
import org.unisg.ftengrave.factorysimulator.domain.MachineStatus;

public class OneWayPointToPointTransportService {

  private static final Duration SOURCE_POLL_INTERVAL = Duration.ofMillis(50);

  private final FactorySimulatorService factorySimulatorService;
  private final Duration movementDelay;
  private final OneWayTransportMachine machine;
  private final AtomicReference<MachineStatus> status;

  public OneWayPointToPointTransportService(
      FactorySimulatorService factorySimulatorService,
      FactorySimulationProperties properties,
      String machineName,
      String acceptedStart,
      String inputSink,
      List<String> previousSinks,
      String holdSink,
      int statusCardX,
      int statusCardY) {
    this.factorySimulatorService = factorySimulatorService;
    this.movementDelay = properties.getMovementDelay();
    this.machine = new OneWayTransportMachine(
        machineName,
        acceptedStart,
        inputSink,
        List.copyOf(previousSinks),
        holdSink,
        statusCardX,
        statusCardY);
    this.status = new AtomicReference<>(this.machine.idleStatus());
  }

  public OneWayTransportExecution transport(
      String machine,
      String start,
      Function<Item, String> targetSinkResolver) {
    validate(machine, start);
    status.set(this.machine.status("Belt on"));

    LocalDateTime startTime = LocalDateTime.now();
    try {
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
    } finally {
      if (factorySimulatorService.getSink(this.machine.holdSink()).item() == null) {
        status.set(this.machine.idleStatus());
      }
    }

    LocalDateTime endTime = LocalDateTime.now();
    return new OneWayTransportExecution(startTime, endTime, Duration.between(startTime, endTime));
  }

  public OneWayTransportExecution detectColor(String machine) {
    validateMachine(machine);
    status.set(this.machine.status("Belt on"));

    LocalDateTime startTime = LocalDateTime.now();
    String detectedColor = "none";
    try {
      Item item = factorySimulatorService.getSink(this.machine.inputSink()).item();
      if (item == null && advancePreviousSinkToInput()) {
        item = factorySimulatorService.getSink(this.machine.inputSink()).item();
      }

      if (item != null) {
        detectedColor = item.color().name().toLowerCase(Locale.ENGLISH);
      }
    } finally {
      if (factorySimulatorService.getSink(this.machine.holdSink()).item() == null) {
        status.set(this.machine.idleStatus());
      }
    }

    LocalDateTime endTime = LocalDateTime.now();
    return new OneWayTransportExecution(
        startTime, endTime, Duration.between(startTime, endTime), detectedColor);
  }

  public MachineStatus getStatus() {
    return status.get();
  }

  public OneWayTransportExecution setMotorSpeed(String machine, int motor, int speed) {
    validateMachine(machine);

    LocalDateTime startTime = LocalDateTime.now();
    if (speed > 0) {
      status.set(this.machine.status("Belt on"));
    } else {
      status.set(this.machine.idleStatus());
    }
    LocalDateTime endTime = LocalDateTime.now();
    return new OneWayTransportExecution(startTime, endTime, Duration.between(startTime, endTime));
  }

  private void validate(String machine, String start) {
    validateMachine(machine);
    if (!this.machine.acceptedStart().equals(start)) {
      throw new IllegalArgumentException("Unknown start for machine " + machine + ": " + start);
    }
  }

  private void validateMachine(String machine) {
    if (!this.machine.name().equals(machine)) {
      throw new IllegalArgumentException("Unsupported one way transport machine: " + machine);
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
      String holdSink,
      int statusCardX,
      int statusCardY) {

    private MachineStatus status(String phase) {
      return new MachineStatus(name, true, phase, statusCardX, statusCardY);
    }

    private MachineStatus idleStatus() {
      return new MachineStatus(name, false, "Belt off", statusCardX, statusCardY);
    }
  }

  public record OneWayTransportExecution(
      LocalDateTime startTime,
      LocalDateTime endTime,
      Duration processTime,
      String detectedColor) {
    public OneWayTransportExecution(
        LocalDateTime startTime,
        LocalDateTime endTime,
        Duration processTime) {
      this(startTime, endTime, processTime, null);
    }
  }

  public record OneWayTransportResponse(
      List<?> attributes,
      String end_time,
      String link,
      String process_time,
      String start_time) {
  }
}
