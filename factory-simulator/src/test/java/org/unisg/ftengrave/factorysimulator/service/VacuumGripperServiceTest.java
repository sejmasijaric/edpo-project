package org.unisg.ftengrave.factorysimulator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNull;

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

    VacuumGripperService service = new VacuumGripperService(factorySimulatorService, properties);

    CompletableFuture<Void> execution = CompletableFuture.runAsync(
        () -> service.pickUpAndTransport("vgr_1", "sink_2", "oven"));

    Thread.sleep(20);
    assertTrue(service.getStatus().moving());
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

    VacuumGripperService service = new VacuumGripperService(factorySimulatorService, properties);

    VacuumGripperService.VacuumGripperExecution response =
        service.pickUpAndTransport("vgr_1", "start", "end");

    assertTrue(!response.processTime().isNegative());
    assertNull(sink(factorySimulatorService, "VGR-Hold").item());
    assertEquals("ITEM-1001", sink(factorySimulatorService, "SINK-I2").item().id());
  }

  private Sink sink(FactorySimulatorService service, String sinkId) {
    return service.getSinks().stream()
        .filter(sink -> sink.id().equals(sinkId))
        .findFirst()
        .orElseThrow();
  }
}
