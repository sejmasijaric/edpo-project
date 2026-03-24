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

class OneWayPointToPointTransportServiceTest {

  @Test
  void sortsTheItemFromTheInputSink() {
    FactorySimulatorService factorySimulatorService = new FactorySimulatorService();
    factorySimulatorService.addItem("ITEM-1001", ItemColor.Red, "SM-I");

    OneWayPointToPointTransportService service = sorterService(factorySimulatorService, Duration.ZERO);

    OneWayPointToPointTransportService.OneWayTransportExecution response =
        service.transport("sm_1", "initial", item -> "SINK-S1");

    assertTrue(!response.processTime().isNegative());
    assertEquals("Belt off", service.getStatus().phase());
    assertNull(sink(factorySimulatorService, "SM-I").item());
    assertNull(sink(factorySimulatorService, "SM-Hold").item());
    assertEquals("ITEM-1001", sink(factorySimulatorService, "SINK-S1").item().id());
  }

  @Test
  void sortsTheItemFromThePreviousBeltSinkWhenInputIsEmpty() {
    FactorySimulatorService factorySimulatorService = new FactorySimulatorService();
    factorySimulatorService.addItem("ITEM-1001", ItemColor.White, "MM-ejection");

    OneWayPointToPointTransportService service = sorterService(factorySimulatorService, Duration.ZERO);

    service.transport("sm_1", "initial", item -> "SINK-S2");

    assertNull(sink(factorySimulatorService, "MM-ejection").item());
    assertNull(sink(factorySimulatorService, "SM-I").item());
    assertEquals("ITEM-1001", sink(factorySimulatorService, "SINK-S2").item().id());
  }

  @Test
  void advancesThePreviousBeltItemToTheInputSinkAfterSorting() {
    FactorySimulatorService factorySimulatorService = new FactorySimulatorService();
    factorySimulatorService.addItem("ITEM-1001", ItemColor.Blue, "SM-I");
    factorySimulatorService.addItem("ITEM-1002", ItemColor.Red, "MM-ejection");

    OneWayPointToPointTransportService service = sorterService(factorySimulatorService, Duration.ZERO);

    service.transport("sm_1", "initial", item -> "SINK-S3");

    assertEquals("ITEM-1001", sink(factorySimulatorService, "SINK-S3").item().id());
    assertEquals("ITEM-1002", sink(factorySimulatorService, "SM-I").item().id());
    assertNull(sink(factorySimulatorService, "MM-ejection").item());
  }

  @Test
  void blocksUntilAnItemArrives() throws Exception {
    FactorySimulatorService factorySimulatorService = new FactorySimulatorService();
    OneWayPointToPointTransportService service = sorterService(factorySimulatorService, Duration.ZERO);

    CompletableFuture<Void> execution = CompletableFuture.runAsync(
        () -> service.transport("sm_1", "initial", item -> "SINK-S1"));

    Thread.sleep(120);
    assertTrue(!execution.isDone());
    assertTrue(service.getStatus().performingAction());
    assertEquals("Belt on", service.getStatus().phase());

    factorySimulatorService.addItem("ITEM-1001", ItemColor.Red, "MM-ejection");

    execution.get(1, TimeUnit.SECONDS);
    assertEquals("Belt off", service.getStatus().phase());
    assertEquals("ITEM-1001", sink(factorySimulatorService, "SINK-S1").item().id());
  }

  @Test
  void keepsTheBeltOnWhenTheItemRemainsInHold() {
    FactorySimulatorService factorySimulatorService = new FactorySimulatorService();
    factorySimulatorService.addItem("ITEM-1001", ItemColor.Red, "SM-I");
    factorySimulatorService.addItem("ITEM-1002", ItemColor.Blue, "SINK-S1");

    OneWayPointToPointTransportService service = sorterService(factorySimulatorService, Duration.ZERO);

    service.transport("sm_1", "initial", item -> "SINK-S1");

    assertTrue(service.getStatus().performingAction());
    assertEquals("Belt on", service.getStatus().phase());
    assertEquals("ITEM-1001", sink(factorySimulatorService, "SM-Hold").item().id());
  }

  @Test
  void setMotorSpeedControlsTheBeltStatus() {
    FactorySimulatorService factorySimulatorService = new FactorySimulatorService();
    OneWayPointToPointTransportService service = sorterService(factorySimulatorService, Duration.ZERO);

    service.setMotorSpeed("sm_1", 1, 400);
    assertTrue(service.getStatus().performingAction());
    assertEquals("Belt on", service.getStatus().phase());

    service.setMotorSpeed("sm_1", 1, 0);
    assertTrue(!service.getStatus().performingAction());
    assertEquals("Belt off", service.getStatus().phase());
  }

  private OneWayPointToPointTransportService sorterService(
      FactorySimulatorService factorySimulatorService,
      Duration movementDelay) {
    FactorySimulationProperties properties = new FactorySimulationProperties();
    properties.setMovementDelay(movementDelay);
    return new OneWayPointToPointTransportService(
        factorySimulatorService,
        properties,
        "sm_1",
        "initial",
        "SM-I",
        java.util.List.of("MM-ejection"),
        "SM-Hold",
        880,
        410);
  }

  private Sink sink(FactorySimulatorService service, String sinkId) {
    return service.getSinks().stream()
        .filter(sink -> sink.id().equals(sinkId))
        .findFirst()
        .orElseThrow();
  }
}
