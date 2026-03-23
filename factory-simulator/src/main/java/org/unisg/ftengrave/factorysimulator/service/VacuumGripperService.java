package org.unisg.ftengrave.factorysimulator.service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.unisg.ftengrave.factorysimulator.domain.MachineStatus;
public class VacuumGripperService {

  private final FactorySimulatorService factorySimulatorService;
  private final Duration movementDelay;
  private final VacuumGripperMachine machine;
  private final AtomicReference<MachineStatus> status;

  public VacuumGripperService(
      FactorySimulatorService factorySimulatorService,
      FactorySimulationProperties properties,
      String machineName,
      String holdSink,
      int statusCardX,
      int statusCardY,
      Map<String, String> requestToFactorySinkMapping) {
    this.factorySimulatorService = factorySimulatorService;
    this.movementDelay = properties.getMovementDelay();
    this.machine = new VacuumGripperMachine(
        machineName,
        holdSink,
        statusCardX,
        statusCardY,
        Map.copyOf(requestToFactorySinkMapping));
    this.status = new AtomicReference<>(this.machine.idleStatus());
  }

  public VacuumGripperExecution pickUpAndTransport(String machine, String start, String end) {
    if (!this.machine.name().equals(machine)) {
      throw new IllegalArgumentException("Unsupported vacuum gripper machine: " + machine);
    }

    String mappedStart = this.machine.mapSink(start);
    String mappedEnd = this.machine.mapSink(end);
    LocalDateTime startTime = LocalDateTime.now();

    try {
      status.set(this.machine.status("Moving to pickup"));
      waitBetweenMovements();
      factorySimulatorService.moveItemBetweenSinks(mappedStart, this.machine.holdSink(), true);

      status.set(this.machine.status("Moving to drop-off"));
      waitBetweenMovements();
      factorySimulatorService.moveItemBetweenSinks(this.machine.holdSink(), mappedEnd, true);
    } finally {
      status.set(this.machine.idleStatus());
    }

    LocalDateTime endTime = LocalDateTime.now();
    return new VacuumGripperExecution(startTime, endTime, Duration.between(startTime, endTime));
  }

  public MachineStatus getStatus() {
    return status.get();
  }

  private void waitBetweenMovements() {
    try {
      Thread.sleep(movementDelay.toMillis());
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Vacuum gripper motion was interrupted", exception);
    }
  }

  private record VacuumGripperMachine(
      String name,
      String holdSink,
      int statusCardX,
      int statusCardY,
      Map<String, String> requestToFactorySinkMapping) {

    private String mapSink(String requestSink) {
      String factorySink = requestToFactorySinkMapping.get(requestSink);
      if (factorySink == null) {
        throw new IllegalArgumentException(
            "Unknown sink for machine " + name + ": " + requestSink);
      }
      return factorySink;
    }

    private MachineStatus status(String phase) {
      return new MachineStatus(name, true, phase, statusCardX, statusCardY);
    }

    private MachineStatus idleStatus() {
      return new MachineStatus(name, false, "Idle", statusCardX, statusCardY);
    }
  }

  public record VacuumGripperExecution(
      LocalDateTime startTime,
      LocalDateTime endTime,
      Duration processTime) {
  }

  public record VacuumGripperResponse(
      List<String> attributes,
      String end_time,
      String link,
      String process_time,
      String start_time) {
  }
}
