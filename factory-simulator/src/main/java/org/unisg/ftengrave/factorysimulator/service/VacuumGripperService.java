package org.unisg.ftengrave.factorysimulator.service;

import java.time.LocalDateTime;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class VacuumGripperService {

  private static final String VGR_1 = "vgr_1";
  private static final String HOLD_SINK = "VGR-Hold";

  private final FactorySimulatorService factorySimulatorService;
  private final Duration movementDelay;
  private final Map<String, VacuumGripperMachine> machines;

  public VacuumGripperService(
      FactorySimulatorService factorySimulatorService,
      FactorySimulationProperties properties) {
    this.factorySimulatorService = factorySimulatorService;
    this.movementDelay = properties.getMovementDelay();
    this.machines = Map.of(
        VGR_1,
        new VacuumGripperMachine(
            VGR_1,
            HOLD_SINK,
            createSinkMapping(
                "oven", "VGR-oven",
                "start", "SINK-I1",
                "end", "SINK-I2",
                "sink_1", "SINK-S1",
                "sink_2", "SINK-S2",
                "sink_3", "SINK-S3")));
  }

  public VacuumGripperExecution pickUpAndTransport(String machine, String start, String end) {
    VacuumGripperMachine selectedMachine = machines.get(machine);
    if (selectedMachine == null) {
      throw new IllegalArgumentException("Unsupported vacuum gripper machine: " + machine);
    }

    String mappedStart = selectedMachine.mapSink(start);
    String mappedEnd = selectedMachine.mapSink(end);
    LocalDateTime startTime = LocalDateTime.now();

    waitBetweenMovements();
    factorySimulatorService.moveItemBetweenSinks(mappedStart, selectedMachine.holdSink(), true);

    waitBetweenMovements();
    factorySimulatorService.moveItemBetweenSinks(selectedMachine.holdSink(), mappedEnd, true);

    LocalDateTime endTime = LocalDateTime.now();
    return new VacuumGripperExecution(startTime, endTime, Duration.between(startTime, endTime));
  }

  private void waitBetweenMovements() {
    try {
      Thread.sleep(movementDelay.toMillis());
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Vacuum gripper motion was interrupted", exception);
    }
  }

  private static Map<String, String> createSinkMapping(String... entries) {
    Map<String, String> mapping = new LinkedHashMap<>();
    for (int index = 0; index < entries.length; index += 2) {
      mapping.put(entries[index], entries[index + 1]);
    }
    return Map.copyOf(mapping);
  }

  private record VacuumGripperMachine(
      String name,
      String holdSink,
      Map<String, String> requestToFactorySinkMapping) {

    private String mapSink(String requestSink) {
      String factorySink = requestToFactorySinkMapping.get(requestSink);
      if (factorySink == null) {
        throw new IllegalArgumentException(
            "Unknown sink for machine " + name + ": " + requestSink);
      }
      return factorySink;
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
