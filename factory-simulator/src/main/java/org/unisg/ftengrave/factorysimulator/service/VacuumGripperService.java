package org.unisg.ftengrave.factorysimulator.service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import org.unisg.ftengrave.factorysimulator.domain.MachineStatus;

public class VacuumGripperService {

  private final FactorySimulatorService factorySimulatorService;
  private final Duration movementDelay;
  private final VacuumGripperMachine machine;
  private final AtomicReference<MachineStatus> status;
  private final AtomicReference<VacuumGripperTaskState> mqttStatus;
  private final AtomicBoolean movementFailureSimulationEnabled;

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
    this.mqttStatus = new AtomicReference<>(VacuumGripperTaskState.idle());
    this.movementFailureSimulationEnabled = new AtomicBoolean(false);
  }

  public VacuumGripperExecution pickUpAndTransport(String machine, String start, String end) {
    if (!this.machine.name().equals(machine)) {
      throw new IllegalArgumentException("Unsupported vacuum gripper machine: " + machine);
    }

    String mappedStart = this.machine.mapSink(start);
    String mappedEnd = this.machine.mapSink(end);
    LocalDateTime startTime = LocalDateTime.now();

    try {
      mqttStatus.set(VacuumGripperTaskState.started("pick_up_and_transport"));
      status.set(this.machine.status("Moving to pickup"));
      waitBetweenMovements();
      if (!movementFailureSimulationEnabled.get()) {
        factorySimulatorService.moveItemBetweenSinks(mappedStart, this.machine.holdSink(), true);
      }

      status.set(this.machine.status("Moving to drop-off"));
      waitBetweenMovements();
      if (!movementFailureSimulationEnabled.get()) {
        factorySimulatorService.tryMoveItemBetweenSinks(this.machine.holdSink(), mappedEnd, true);
      }
    } finally {
      status.set(this.machine.idleStatus());
      mqttStatus.set(VacuumGripperTaskState.idle());
    }

    LocalDateTime endTime = LocalDateTime.now();
    return new VacuumGripperExecution(startTime, endTime, Duration.between(startTime, endTime));
  }

  public MachineStatus getStatus() {
    return status.get();
  }

  public VacuumGripperMqttStatus getMqttStatus() {
    return mqttStatus.get().snapshot();
  }

  public boolean isMovementFailureSimulationEnabled() {
    return movementFailureSimulationEnabled.get();
  }

  public void setMovementFailureSimulationEnabled(boolean enabled) {
    movementFailureSimulationEnabled.set(enabled);
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

  public record VacuumGripperMqttStatus(
      String currentState,
      String currentTask,
      double currentTaskDurationSeconds) {
  }

  private record VacuumGripperTaskState(String currentTask, LocalDateTime startedAt) {

    private static VacuumGripperTaskState started(String currentTask) {
      return new VacuumGripperTaskState(currentTask, LocalDateTime.now());
    }

    private static VacuumGripperTaskState idle() {
      return new VacuumGripperTaskState("", null);
    }

    private VacuumGripperMqttStatus snapshot() {
      if (startedAt == null || currentTask.isBlank()) {
        return new VacuumGripperMqttStatus("ready", "", 0.0d);
      }

      long elapsedMillis = Duration.between(startedAt, LocalDateTime.now()).toMillis();
      return new VacuumGripperMqttStatus("busy", currentTask, elapsedMillis / 1000.0d);
    }
  }
}
