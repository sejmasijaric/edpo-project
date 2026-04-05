package org.unisg.ftengrave.factorysimulator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factorysimulator.domain.ItemColor;
import org.unisg.ftengrave.factorysimulator.domain.Sink;

class VacuumGripperServiceTest {

  @Test
  void fetchesTheItemFromHoldAgainForTheSecondMove() throws Exception {
    FactorySimulatorService factorySimulatorService = new FactorySimulatorService();
    factorySimulatorService.addItem("ITEM-1001", ItemColor.Red, "SINK-S2");

    FactorySimulationProperties properties = new FactorySimulationProperties();
    properties.setMovementDelay(Duration.ofMillis(80));

    VacuumGripperService service = new VacuumGripperService(
        factorySimulatorService,
        properties,
        "vgr_1",
        "VGR-Hold",
        530,
        360,
        java.util.Map.of(
            "oven", "VGR-oven",
            "start", "SINK-I1",
            "end", "SINK-I2",
            "sink_1", "SINK-S1",
            "sink_2", "SINK-S2",
            "sink_3", "SINK-S3"));

    CompletableFuture<Void> execution = CompletableFuture.runAsync(
        () -> service.pickUpAndTransport("vgr_1", "sink_2", "oven"));

    Thread.sleep(20);
    assertTrue(service.getStatus().performingAction());
    Thread.sleep(100);
    factorySimulatorService.deleteItem("ITEM-1001");
    execution.get(1, TimeUnit.SECONDS);

    assertEquals("Idle", service.getStatus().phase());
    assertNull(sink(factorySimulatorService, "VGR-Hold").item());
    assertNull(sink(factorySimulatorService, "VGR-oven").item());
  }

  @Test
  void movesTheItemToTheConfiguredEndSink() {
    FactorySimulatorService factorySimulatorService = new FactorySimulatorService();
    factorySimulatorService.addItem("ITEM-1001", ItemColor.Red, "SINK-I1");

    FactorySimulationProperties properties = new FactorySimulationProperties();
    properties.setMovementDelay(Duration.ZERO);

    VacuumGripperService service = new VacuumGripperService(
        factorySimulatorService,
        properties,
        "vgr_1",
        "VGR-Hold",
        530,
        360,
        java.util.Map.of(
            "oven", "VGR-oven",
            "start", "SINK-I1",
            "end", "SINK-I2",
            "sink_1", "SINK-S1",
            "sink_2", "SINK-S2",
            "sink_3", "SINK-S3"));

    VacuumGripperService.VacuumGripperExecution response =
        service.pickUpAndTransport("vgr_1", "start", "end");

    assertTrue(!response.processTime().isNegative());
    assertEquals("", service.getMqttStatus().currentTask());
    assertEquals(0.0d, service.getMqttStatus().currentTaskDurationSeconds());
    assertNull(sink(factorySimulatorService, "VGR-Hold").item());
    assertEquals("ITEM-1001", sink(factorySimulatorService, "SINK-I2").item().id());
  }

  @Test
  void supportsTheWorkstationTransportMapping() {
    FactorySimulatorService factorySimulatorService = new FactorySimulatorService();
    factorySimulatorService.addItem("ITEM-2001", ItemColor.Blue, "MM-initial");

    FactorySimulationProperties properties = new FactorySimulationProperties();
    properties.setMovementDelay(Duration.ZERO);

    VacuumGripperService service = new VacuumGripperService(
        factorySimulatorService,
        properties,
        "wt_1",
        "WT-Hold",
        620,
        120,
        java.util.Map.of(
            "oven", "VGR-oven",
            "milling_machine", "MM-initial"));

    VacuumGripperService.VacuumGripperExecution response =
        service.pickUpAndTransport("wt_1", "milling_machine", "oven");

    assertTrue(!response.processTime().isNegative());
    assertNull(sink(factorySimulatorService, "WT-Hold").item());
    assertEquals("ITEM-2001", sink(factorySimulatorService, "VGR-oven").item().id());
  }

  @Test
  void leavesTheItemInHoldWhenTheDropOffSinkIsOccupied() {
    FactorySimulatorService factorySimulatorService = new FactorySimulatorService();
    factorySimulatorService.addItem("ITEM-1001", ItemColor.Red, "SINK-I1");
    factorySimulatorService.addItem("ITEM-1002", ItemColor.Blue, "SINK-I2");

    FactorySimulationProperties properties = new FactorySimulationProperties();
    properties.setMovementDelay(Duration.ZERO);

    VacuumGripperService service = new VacuumGripperService(
        factorySimulatorService,
        properties,
        "vgr_1",
        "VGR-Hold",
        530,
        360,
        java.util.Map.of(
            "oven", "VGR-oven",
            "start", "SINK-I1",
            "end", "SINK-I2",
            "sink_1", "SINK-S1",
            "sink_2", "SINK-S2",
            "sink_3", "SINK-S3"));

    VacuumGripperService.VacuumGripperExecution response =
        service.pickUpAndTransport("vgr_1", "start", "end");

    assertTrue(!response.processTime().isNegative());
    assertNull(sink(factorySimulatorService, "SINK-I1").item());
    assertEquals("ITEM-1001", sink(factorySimulatorService, "VGR-Hold").item().id());
    assertEquals("ITEM-1002", sink(factorySimulatorService, "SINK-I2").item().id());
  }

  @Test
  void skipsSinkTransfersWhenMovementFailureSimulationIsEnabled() {
    FactorySimulatorService factorySimulatorService = new FactorySimulatorService();
    factorySimulatorService.addItem("ITEM-1001", ItemColor.Red, "SINK-I1");

    FactorySimulationProperties properties = new FactorySimulationProperties();
    properties.setMovementDelay(Duration.ZERO);

    VacuumGripperService service = new VacuumGripperService(
        factorySimulatorService,
        properties,
        "vgr_1",
        "VGR-Hold",
        530,
        360,
        java.util.Map.of(
            "oven", "VGR-oven",
            "start", "SINK-I1",
            "end", "SINK-I2",
            "sink_1", "SINK-S1",
            "sink_2", "SINK-S2",
            "sink_3", "SINK-S3"));
    service.setMovementFailureSimulationEnabled(true);

    service.pickUpAndTransport("vgr_1", "start", "end");

    assertEquals("ITEM-1001", sink(factorySimulatorService, "SINK-I1").item().id());
    assertNull(sink(factorySimulatorService, "VGR-Hold").item());
    assertNull(sink(factorySimulatorService, "SINK-I2").item());
  }

  private Sink sink(FactorySimulatorService service, String sinkId) {
    return service.getSinks().stream()
        .filter(sink -> sink.id().equals(sinkId))
        .findFirst()
        .orElseThrow();
  }
}
