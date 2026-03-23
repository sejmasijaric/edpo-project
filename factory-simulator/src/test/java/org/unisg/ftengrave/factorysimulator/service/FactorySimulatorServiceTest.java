package org.unisg.ftengrave.factorysimulator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factorysimulator.domain.ItemColor;
import org.unisg.ftengrave.factorysimulator.domain.ManagedItem;
import org.unisg.ftengrave.factorysimulator.domain.Sink;

class FactorySimulatorServiceTest {

  private final FactorySimulatorService service = new FactorySimulatorService();

  @Test
  void listsItemsWithTheirCurrentSink() {
    service.addItem("ITEM-1001", ItemColor.Red, "SINK-I1");
    service.addItem("ITEM-1002", ItemColor.White, "SINK-S2");

    assertEquals(
        List.of(
            new ManagedItem("ITEM-1001", ItemColor.Red, "SINK-I1"),
            new ManagedItem("ITEM-1002", ItemColor.White, "SINK-S2")),
        service.getItems());
  }

  @Test
  void addsAnItemToAnEmptySink() {
    service.addItem("ITEM-2001", ItemColor.Blue, "SINK-S1");

    Sink targetSink = sink("SINK-S1");
    assertNotNull(targetSink.item());
    assertEquals("ITEM-2001", targetSink.item().id());
    assertEquals(ItemColor.Blue, targetSink.item().color());
  }

  @Test
  void rejectsAddingAnItemToAnOccupiedSink() {
    service.addItem("ITEM-1001", ItemColor.Blue, "SINK-I1");

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> service.addItem("ITEM-2001", ItemColor.Blue, "SINK-I1"));

    assertEquals("Target sink already contains an item", exception.getMessage());
  }

  @Test
  void rejectsAddingDuplicateItems() {
    service.addItem("ITEM-1001", ItemColor.Blue, "SINK-I1");

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> service.addItem("ITEM-1001", ItemColor.Blue, "SINK-S1"));

    assertEquals("Item already exists: ITEM-1001", exception.getMessage());
  }

  @Test
  void movesItemToAnEmptySink() {
    service.addItem("ITEM-1001", ItemColor.Red, "SINK-I1");

    service.moveItem("ITEM-1001", "SINK-S1");

    assertNull(sink("SINK-I1").item());
    assertEquals("ITEM-1001", sink("SINK-S1").item().id());
  }

  @Test
  void rejectsMovingItemToAnOccupiedSink() {
    service.addItem("ITEM-1001", ItemColor.Red, "SINK-I1");
    service.addItem("ITEM-1002", ItemColor.Blue, "SINK-I2");

    IllegalStateException exception = assertThrows(IllegalStateException.class,
        () -> service.moveItem("ITEM-1001", "SINK-I2"));

    assertEquals("Target sink already contains an item", exception.getMessage());
  }

  @Test
  void canLeaveTheItemInPlaceWhenTryingToMoveToAnOccupiedSink() {
    service.addItem("ITEM-1001", ItemColor.Red, "VGR-Hold");
    service.addItem("ITEM-1002", ItemColor.Blue, "SINK-I2");

    boolean moved = service.tryMoveItemBetweenSinks("VGR-Hold", "SINK-I2", false);

    assertEquals(false, moved);
    assertEquals("ITEM-1001", sink("VGR-Hold").item().id());
    assertEquals("ITEM-1002", sink("SINK-I2").item().id());
  }

  @Test
  void rejectsUnknownItems() {
    assertThrows(NoSuchElementException.class, () -> service.moveItem("ITEM-404", "SINK-S1"));
  }

  @Test
  void deletesAnExistingItem() {
    service.addItem("ITEM-1002", ItemColor.White, "SINK-S2");

    service.deleteItem("ITEM-1002");

    assertNull(sink("SINK-S2").item());
    assertEquals(List.of(), service.getItems());
  }

  @Test
  void ignoresMissingSourceItemsWhenConfigured() {
    service.moveItemBetweenSinks("SINK-I1", "VGR-Hold", true);

    assertNull(sink("SINK-I1").item());
    assertNull(sink("VGR-Hold").item());
  }

  @Test
  void rejectsMissingSourceItemsWhenConfiguredNotToIgnoreThem() {
    NoSuchElementException exception = assertThrows(NoSuchElementException.class,
        () -> service.moveItemBetweenSinks("SINK-I1", "VGR-Hold", false));

    assertEquals("Source sink does not contain an item: SINK-I1", exception.getMessage());
  }

  private Sink sink(String sinkId) {
    return service.getSinks().stream()
        .filter(sink -> sink.id().equals(sinkId))
        .findFirst()
        .orElseThrow();
  }
}
