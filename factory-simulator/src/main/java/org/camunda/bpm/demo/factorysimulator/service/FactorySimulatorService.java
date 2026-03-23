package org.camunda.bpm.demo.factorysimulator.service;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import org.camunda.bpm.demo.factorysimulator.model.Item;
import org.camunda.bpm.demo.factorysimulator.model.ItemColor;
import org.camunda.bpm.demo.factorysimulator.model.ManagedItem;
import org.camunda.bpm.demo.factorysimulator.model.Sink;
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

  public synchronized List<ManagedItem> getItems() {
    return sinks.values().stream()
        .filter(sink -> sink.item() != null)
        .map(sink -> new ManagedItem(sink.item().id(), sink.item().color(), sink.id()))
        .sorted(Comparator.comparing(ManagedItem::id))
        .toList();
  }

  public synchronized void moveItem(String itemId, String targetSinkId) {
    String currentSinkId = findSinkContainingItem(itemId);
    if (currentSinkId.equals(targetSinkId)) {
      return;
    }

    Sink targetSink = sinks.get(targetSinkId);
    if (targetSink == null) {
      throw new NoSuchElementException("Unknown target sink: " + targetSinkId);
    }
    if (targetSink.item() != null) {
      throw new IllegalStateException("Target sink already contains an item");
    }

    Sink sourceSink = sinks.get(currentSinkId);
    Item item = sourceSink.item();

    sinks.put(currentSinkId, sourceSink.withItem(null));
    sinks.put(targetSinkId, targetSink.withItem(item));
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
    sinks.put("SINK-A1", new Sink("SINK-A1", 120, 110, new Item("ITEM-1001", ItemColor.Red)));
    sinks.put("SINK-A2", new Sink("SINK-A2", 330, 110, new Item("ITEM-1002", ItemColor.Red)));
    sinks.put("SINK-B1", new Sink("SINK-B1", 180, 300, null));
    sinks.put("SINK-B2", new Sink("SINK-B2", 470, 250, new Item("ITEM-1003", ItemColor.White)));
    sinks.put("SINK-C1", new Sink("SINK-C1", 620, 420, null));
    sinks.put("SINK-C2", new Sink("SINK-C2", 800, 180, null));
  }
}
