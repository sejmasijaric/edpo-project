package org.unisg.ftengrave.dashboardservice.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.streams.kstream.ValueTransformerWithKey;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.unisg.ftengrave.dashboardservice.dto.DashboardEvent;
import org.unisg.ftengrave.dashboardservice.dto.ItemState;

public class DashboardStateTransformer implements ValueTransformerWithKey<String, DashboardEvent, DashboardEvent> {

  public static final String DASHBOARD_EVENTS_STORE = "dashboard-events-store";
  public static final String DASHBOARD_ITEMS_STORE = "dashboard-items-store";

  private final ObjectMapper objectMapper;
  private KeyValueStore<String, String> eventsStore;
  private KeyValueStore<String, String> itemsStore;

  public DashboardStateTransformer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  @SuppressWarnings("unchecked")
  public void init(ProcessorContext context) {
    this.eventsStore = (KeyValueStore<String, String>) context.getStateStore(DASHBOARD_EVENTS_STORE);
    this.itemsStore = (KeyValueStore<String, String>) context.getStateStore(DASHBOARD_ITEMS_STORE);
  }

  @Override
  public DashboardEvent transform(String readOnlyKey, DashboardEvent event) {
    if (event == null || event.eventId() == null) {
      return event;
    }
    put(eventsStore, event.eventId(), event);

    if (event.itemIdentifier() != null && !event.itemIdentifier().isBlank()) {
      ItemState state = readItem(event.itemIdentifier());
      apply(state, event);
      put(itemsStore, event.itemIdentifier(), state);
    }
    return event;
  }

  private void apply(ItemState state, DashboardEvent event) {
    switch (event.eventType()) {
      case DashboardEventNormalizer.ORDER_CREATED -> {
        state.setEndToEndStartTimestamp(firstKnown(state.getEndToEndStartTimestamp(), event.timestamp()));
        state.setCurrentStage("INTAKE");
      }
      case DashboardEventNormalizer.RUN_ITEM_INTAKE_COMMAND -> {
        state.setEndToEndStartTimestamp(firstKnown(state.getEndToEndStartTimestamp(), event.timestamp()));
        state.setCurrentStage("INTAKE");
        countAttempt(state, event.eventType());
      }
      case DashboardEventNormalizer.RUN_PRODUCTION_COMMAND -> {
        state.setProductionStartTimestamp(event.timestamp());
        state.setCurrentStage("MANUFACTURING");
        countAttempt(state, event.eventType());
      }
      case DashboardEventNormalizer.RUN_ITEM_QC_COMMAND -> {
        state.setCurrentStage("QC");
        countAttempt(state, event.eventType());
      }
      case DashboardEventNormalizer.MANUAL_INTERVENTION_ISSUED -> {
        state.setManualInterventionOpen(true);
        state.setOpenTaskName(event.taskName());
        state.setCurrentStage("MANUAL_INTERVENTION");
      }
      case DashboardEventNormalizer.MANUAL_INTERVENTION_COMPLETED -> {
        state.setManualInterventionOpen(false);
        state.setOpenTaskName(null);
      }
      case DashboardEventNormalizer.QC_SHIPPING, DashboardEventNormalizer.QC_REJECTION -> {
        state.setTerminal(true);
        state.setTerminalTimestamp(event.timestamp());
        state.setTerminalOutcome(event.eventType());
        state.setCurrentStage(null);
        state.setManualInterventionOpen(false);
        state.setOpenTaskName(null);
      }
      default -> {
        if (event.eventType() != null && event.eventType().endsWith("-command")) {
          countAttempt(state, event.eventType());
        }
      }
    }
  }

  private Long firstKnown(Long current, long next) {
    return current == null ? next : Math.min(current, next);
  }

  private void countAttempt(ItemState state, String eventType) {
    int count = state.getAttemptCounts().getOrDefault(eventType, 0) + 1;
    state.getAttemptCounts().put(eventType, count);
    if (count > 1) {
      state.setRetryCount(state.getRetryCount() + 1);
    }
  }

  private ItemState readItem(String itemIdentifier) {
    String existing = itemsStore.get(itemIdentifier);
    if (existing == null) {
      return new ItemState(itemIdentifier);
    }
    try {
      return objectMapper.readValue(existing, ItemState.class);
    } catch (Exception exception) {
      return new ItemState(itemIdentifier);
    }
  }

  private void put(KeyValueStore<String, String> store, String key, Object value) {
    try {
      store.put(key, objectMapper.writeValueAsString(value));
    } catch (Exception exception) {
      throw new IllegalStateException("Could not write dashboard state", exception);
    }
  }

  @Override
  public void close() {
  }
}
