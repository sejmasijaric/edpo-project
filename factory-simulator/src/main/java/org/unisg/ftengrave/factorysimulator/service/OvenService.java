package org.unisg.ftengrave.factorysimulator.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.unisg.ftengrave.factorysimulator.domain.MachineStatus;

public class OvenService {

  private final OvenMachine machine;
  private final Duration defaultBurnDuration;
  private final AtomicReference<MachineStatus> status;
  private final AtomicReference<OvenTaskState> mqttStatus;

  public OvenService(
      FactorySimulationProperties properties,
      String machineName,
      int statusCardX,
      int statusCardY) {
    this.machine = new OvenMachine(machineName, statusCardX, statusCardY);
    this.defaultBurnDuration = properties.getOvenBurnDuration();
    this.status = new AtomicReference<>(this.machine.idleStatus());
    this.mqttStatus = new AtomicReference<>(OvenTaskState.idle());
  }

  public OvenExecution burn(String machine, Integer timeInSeconds) {
    validateMachine(machine);

    Duration burnDuration = resolveBurnDuration(timeInSeconds);
    LocalDateTime startTime = LocalDateTime.now();

    try {
      mqttStatus.set(OvenTaskState.started("burn"));
      status.set(this.machine.status("burn"));
      waitForBurn(burnDuration);
    } finally {
      status.set(this.machine.idleStatus());
      mqttStatus.set(OvenTaskState.idle());
    }

    LocalDateTime endTime = LocalDateTime.now();
    return new OvenExecution(startTime, endTime, Duration.between(startTime, endTime));
  }

  public MachineStatus getStatus() {
    return status.get();
  }

  public OvenMqttStatus getMqttStatus() {
    return mqttStatus.get().snapshot();
  }

  private void validateMachine(String machine) {
    if (!this.machine.name().equals(machine)) {
      throw new IllegalArgumentException("Unsupported oven machine: " + machine);
    }
  }

  private Duration resolveBurnDuration(Integer timeInSeconds) {
    if (timeInSeconds == null) {
      return defaultBurnDuration;
    }
    if (timeInSeconds < 0) {
      throw new IllegalArgumentException("Burn time must be zero or greater");
    }
    return Duration.ofSeconds(timeInSeconds);
  }

  private void waitForBurn(Duration burnDuration) {
    try {
      Thread.sleep(burnDuration.toMillis());
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Oven burn was interrupted", exception);
    }
  }

  private record OvenMachine(
      String name,
      int statusCardX,
      int statusCardY) {

    private MachineStatus status(String phase) {
      return new MachineStatus(name, true, phase, statusCardX, statusCardY);
    }

    private MachineStatus idleStatus() {
      return new MachineStatus(name, false, "Idle", statusCardX, statusCardY);
    }
  }

  public record OvenExecution(
      LocalDateTime startTime,
      LocalDateTime endTime,
      Duration processTime) {
  }

  public record OvenResponse(
      List<String> attributes,
      String end_time,
      String link,
      String process_time,
      String start_time) {
  }

  public record OvenMqttStatus(
      String currentTask,
      double currentTaskDurationSeconds) {
  }

  private record OvenTaskState(String currentTask, LocalDateTime startedAt) {

    private static OvenTaskState started(String currentTask) {
      return new OvenTaskState(currentTask, LocalDateTime.now());
    }

    private static OvenTaskState idle() {
      return new OvenTaskState("", null);
    }

    private OvenMqttStatus snapshot() {
      if (startedAt == null || currentTask.isBlank()) {
        return new OvenMqttStatus("", 0.0d);
      }

      long elapsedMillis = Duration.between(startedAt, LocalDateTime.now()).toMillis();
      return new OvenMqttStatus(currentTask, elapsedMillis / 1000.0d);
    }
  }
}
