package org.unisg.ftengrave.factorysimulator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.NoSuchElementException;
import org.unisg.ftengrave.factorysimulator.model.ManagedItem;
import org.unisg.ftengrave.factorysimulator.model.Sink;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factorysimulator.model.ItemColor;

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

  @Test
  void listsItemsWithTheirCurrentSink() {
    assertEquals(
        java.util.List.of(
            new ManagedItem("ITEM-1001", ItemColor.Red, "SINK-A1"),
            new ManagedItem("ITEM-1002", ItemColor.Red, "SINK-A2"),
            new ManagedItem("ITEM-1003", ItemColor.White, "SINK-B2")),
        service.getItems());
  }

  @Test
  void deletesAnExistingItem() {
    service.deleteItem("ITEM-1002");

    Sink sourceSink = service.getSinks().stream()
        .filter(sink -> sink.id().equals("SINK-A2"))
        .findFirst()
        .orElseThrow();

    assertNull(sourceSink.item());
    assertEquals(
        java.util.List.of("ITEM-1001", "ITEM-1003"),
        service.getItems().stream().map(ManagedItem::id).toList());
  }

  @Test
  void addsAnItemToAnEmptySink() {
    service.addItem("ITEM-2001", ItemColor.Blue, "SINK-B1");

    Sink targetSink = service.getSinks().stream()
        .filter(sink -> sink.id().equals("SINK-B1"))
        .findFirst()
        .orElseThrow();

    assertNotNull(targetSink.item());
    assertEquals("ITEM-2001", targetSink.item().id());
    assertEquals(ItemColor.Blue, targetSink.item().color());
  }

  @Test
  void rejectsAddingAnItemToAnOccupiedSink() {
    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> service.addItem("ITEM-2001", ItemColor.Blue, "SINK-A1"));

    assertEquals("Target sink already contains an item", exception.getMessage());
  }

  @Test
  void rejectsAddingDuplicateItems() {
    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> service.addItem("ITEM-1001", ItemColor.Blue, "SINK-B1"));

    assertEquals("Item already exists: ITEM-1001", exception.getMessage());
  }
}
