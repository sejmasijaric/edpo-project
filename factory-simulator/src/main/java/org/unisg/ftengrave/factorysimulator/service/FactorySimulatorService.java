package org.unisg.ftengrave.factorysimulator.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.unisg.ftengrave.factorysimulator.domain.Item;
import org.unisg.ftengrave.factorysimulator.domain.ItemColor;
import org.unisg.ftengrave.factorysimulator.domain.ManagedItem;
import org.unisg.ftengrave.factorysimulator.domain.Sink;
import org.springframework.stereotype.Service;

@Service
public class FactorySimulatorService {

  private final Map<String, Sink> sinks = new LinkedHashMap<>();

  public FactorySimulatorService() {
    initializeSinks();
  }

  public synchronized List<Sink> getSinks() {
    return sinks.values().stream()
        .sorted(Comparator.comparing(Sink::id))
        .toList();
  }

  public synchronized Sink getSink(String sinkId) {
    Sink sink = sinks.get(sinkId);
    if (sink == null) {
      throw new NoSuchElementException("Unknown sink: " + sinkId);
    }
    return sink;
  }

  public synchronized List<ManagedItem> getItems() {
    return sinks.values().stream()
        .filter(sink -> sink.item() != null)
        .map(sink -> new ManagedItem(sink.item().id(), sink.item().color(), sink.id()))
        .sorted(Comparator.comparing(ManagedItem::id))
        .toList();
  }

  public synchronized void addItem(String itemId, ItemColor color, String sinkId) {
    if (itemId == null || itemId.isBlank()) {
      throw new IllegalArgumentException("Item ID must not be blank");
    }
    if (color == null) {
      throw new IllegalArgumentException("Item color is required");
    }

    Sink sink = sinks.get(sinkId);
    if (sink == null) {
      throw new NoSuchElementException("Unknown target sink: " + sinkId);
    }
    if (sink.item() != null) {
      throw new IllegalStateException("Target sink already contains an item");
    }
    if (sinks.values().stream().anyMatch(existingSink ->
        existingSink.item() != null && existingSink.item().id().equals(itemId))) {
      throw new IllegalStateException("Item already exists: " + itemId);
    }

    sinks.put(sinkId, sink.withItem(new Item(itemId, color)));
  }

  public synchronized void moveItem(String itemId, String targetSinkId) {
    String currentSinkId = findSinkContainingItem(itemId);
    moveItemBetweenSinks(currentSinkId, targetSinkId, false);
  }

  public synchronized void moveItemBetweenSinks(
      String sourceSinkId,
      String targetSinkId,
      boolean ignoreMissingItem) {
    if (!tryMoveItemBetweenSinks(sourceSinkId, targetSinkId, ignoreMissingItem)) {
      throw new IllegalStateException("Target sink already contains an item");
    }
  }

  public synchronized boolean tryMoveItemBetweenSinks(
      String sourceSinkId,
      String targetSinkId,
      boolean ignoreMissingItem) {
    if (sourceSinkId.equals(targetSinkId)) {
      return true;
    }

    Sink sourceSink = sinks.get(sourceSinkId);
    if (sourceSink == null) {
      throw new NoSuchElementException("Unknown source sink: " + sourceSinkId);
    }

    Sink targetSink = sinks.get(targetSinkId);
    if (targetSink == null) {
      throw new NoSuchElementException("Unknown target sink: " + targetSinkId);
    }
    if (targetSink.item() != null) {
      return false;
    }
    if (sourceSink.item() == null) {
      if (ignoreMissingItem) {
        return true;
      }
      throw new NoSuchElementException("Source sink does not contain an item: " + sourceSinkId);
    }

    Item item = sourceSink.item();

    sinks.put(sourceSinkId, sourceSink.withItem(null));
    sinks.put(targetSinkId, targetSink.withItem(item));
    return true;
  }

  public synchronized void deleteItem(String itemId) {
    String currentSinkId = findSinkContainingItem(itemId);
    Sink sourceSink = sinks.get(currentSinkId);
    sinks.put(currentSinkId, sourceSink.withItem(null));
  }

  private String findSinkContainingItem(String itemId) {
    return sinks.values().stream()
        .filter(sink -> sink.item() != null && sink.item().id().equals(itemId))
        .map(Sink::id)
        .findFirst()
        .orElseThrow(() -> new NoSuchElementException("Unknown item: " + itemId));
  }

  private void initializeSinks() {
    sinks.put("SINK-I1", new Sink("SINK-I1", 530, 550, null));
    sinks.put("SINK-I2", new Sink("SINK-I2", 670, 550, null));

    sinks.put("VGR-Hold", new Sink("VGR-Hold", 530, 420, null));

    sinks.put("VGR-oven", new Sink("VGR-oven", 500, 180, null));

    sinks.put("WT-Hold", new Sink("WT-Hold", 620, 180, null));

    sinks.put("MM-initial", new Sink("MM-initial", 740, 180, null));
    sinks.put("MM-ejection", new Sink("MM-ejection", 860, 120, null));

    sinks.put("SM-I", new Sink("SM-I", 850, 330, null));
    sinks.put("SM-Hold", new Sink("SM-Hold", 880, 470, null));

    sinks.put("SINK-S1", new Sink("SINK-S1", 780, 400, null));
    sinks.put("SINK-S2", new Sink("SINK-S2", 780, 470, null));
    sinks.put("SINK-S3", new Sink("SINK-S3", 780, 540, null));
  }
}
