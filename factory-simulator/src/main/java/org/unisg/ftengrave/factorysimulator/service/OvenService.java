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

  public OvenService(
      FactorySimulationProperties properties,
      String machineName,
      int statusCardX,
      int statusCardY) {
    this.machine = new OvenMachine(machineName, statusCardX, statusCardY);
    this.defaultBurnDuration = properties.getOvenBurnDuration();
    this.status = new AtomicReference<>(this.machine.idleStatus());
  }

  public OvenExecution burn(String machine, Integer timeInSeconds) {
    validateMachine(machine);

    Duration burnDuration = resolveBurnDuration(timeInSeconds);
    LocalDateTime startTime = LocalDateTime.now();

    try {
      status.set(this.machine.status("burn"));
      waitForBurn(burnDuration);
    } finally {
      status.set(this.machine.idleStatus());
    }

    LocalDateTime endTime = LocalDateTime.now();
    return new OvenExecution(startTime, endTime, Duration.between(startTime, endTime));
  }

  public MachineStatus getStatus() {
    return status.get();
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
}
