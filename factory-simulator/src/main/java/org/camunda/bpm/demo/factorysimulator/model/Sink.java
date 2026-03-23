package org.camunda.bpm.demo.factorysimulator.model;

public record Sink(String id, int x, int y, Item item) {

  public Sink withItem(Item updatedItem) {
    return new Sink(id, x, y, updatedItem);
  }
}
