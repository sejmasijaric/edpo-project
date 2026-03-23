package org.camunda.bpm.demo.factorysimulator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;
import org.camunda.bpm.demo.factorysimulator.model.Sink;
import org.junit.jupiter.api.Test;

class FactorySimulatorServiceTest {

  private final FactorySimulatorService service = new FactorySimulatorService();

  @Test
  void movesItemToAnEmptySink() {
    service.moveItem("ITEM-1001", "SINK-B1");

    Sink sourceSink = service.getSinks().stream()
        .filter(sink -> sink.id().equals("SINK-A1"))
        .findFirst()
        .orElseThrow();
    Sink targetSink = service.getSinks().stream()
        .filter(sink -> sink.id().equals("SINK-B1"))
        .findFirst()
        .orElseThrow();

    assertNull(sourceSink.item());
    assertNotNull(targetSink.item());
    assertEquals("ITEM-1001", targetSink.item().id());
  }

  @Test
  void rejectsMovingItemToAnOccupiedSink() {
    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> service.moveItem("ITEM-1001", "SINK-A2"));

    assertEquals("Target sink already contains an item", exception.getMessage());
  }

  @Test
  void rejectsUnknownItems() {
    assertThrows(NoSuchElementException.class, () -> service.moveItem("ITEM-404", "SINK-B1"));
  }
}
