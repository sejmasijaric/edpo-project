package org.unisg.ftengrave.dashboardservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StoreQueryParameters;
import org.apache.kafka.streams.errors.InvalidStateStoreException;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.dashboardservice.dto.DashboardEvent;
import org.unisg.ftengrave.dashboardservice.dto.DashboardMetricsResponse;
import org.unisg.ftengrave.dashboardservice.dto.ItemState;
import org.unisg.ftengrave.dashboardservice.streams.DashboardEventNormalizer;
import org.unisg.ftengrave.dashboardservice.streams.DashboardStateTransformer;

@Service
public class DashboardMetricsService {

  private final StreamsBuilderFactoryBean streamsBuilderFactoryBean;
  private final ObjectMapper objectMapper;

  public DashboardMetricsService(
      StreamsBuilderFactoryBean streamsBuilderFactoryBean,
      ObjectMapper objectMapper) {
    this.streamsBuilderFactoryBean = streamsBuilderFactoryBean;
    this.objectMapper = objectMapper;
  }

  public DashboardMetricsResponse metrics(Instant from, Instant to) {
    long fromMillis = from.toEpochMilli();
    long toMillis = to.toEpochMilli();

    List<DashboardEvent> events = eventsInWindow(fromMillis, toMillis);
    List<ItemState> items = itemStates();

    long qcRejected = count(events, DashboardEventNormalizer.QC_REJECTION);
    long qcPassed = count(events, DashboardEventNormalizer.QC_SHIPPING);
    long manufacturingFailed = count(events, DashboardEventNormalizer.MANUFACTURING_FAILED);
    long manufacturingCompleted = count(events, DashboardEventNormalizer.MANUFACTURING_COMPLETED);
    long manualCompleted = count(events, DashboardEventNormalizer.MANUAL_INTERVENTION_COMPLETED);

    List<Long> manufacturingDurations = events.stream()
        .filter(event -> DashboardEventNormalizer.MANUFACTURING_ATTEMPT_DURATION.equals(event.eventType()))
        .map(DashboardEvent::durationMillis)
        .filter(duration -> duration != null && duration >= 0)
        .toList();

    List<Long> endToEndDurations = items.stream()
        .filter(ItemState::isTerminal)
        .filter(item -> item.getEndToEndStartTimestamp() != null && item.getTerminalTimestamp() != null)
        .filter(item -> item.getTerminalTimestamp() >= fromMillis && item.getTerminalTimestamp() <= toMillis)
        .map(item -> Math.max(0, item.getTerminalTimestamp() - item.getEndToEndStartTimestamp()))
        .toList();

    List<DashboardMetricsResponse.OpenIntervention> openInterventions = items.stream()
        .filter(ItemState::isManualInterventionOpen)
        .map(item -> new DashboardMetricsResponse.OpenIntervention(
            item.getItemIdentifier(),
            item.getOpenTaskName(),
            item.getCurrentStage()))
        .toList();

    Map<String, Long> wipByStage = new LinkedHashMap<>();
    wipByStage.put("INTAKE", 0L);
    wipByStage.put("MANUFACTURING", 0L);
    wipByStage.put("QC", 0L);
    wipByStage.put("MANUAL_INTERVENTION", 0L);
    items.stream()
        .filter(item -> !item.isTerminal())
        .filter(item -> item.getCurrentStage() != null && wipByStage.containsKey(item.getCurrentStage()))
        .forEach(item -> wipByStage.computeIfPresent(item.getCurrentStage(), (stage, current) -> current + 1));

    Map<String, Long> retriesPerItem = new HashMap<>();
    items.stream()
        .filter(item -> item.getRetryCount() > 0)
        .forEach(item -> retriesPerItem.put(item.getItemIdentifier(), (long) item.getRetryCount()));
    long completedItems = items.stream()
        .filter(item -> DashboardEventNormalizer.QC_SHIPPING.equals(item.getTerminalOutcome()))
        .count();
    long totalRetries = retriesPerItem.values().stream().mapToLong(Long::longValue).sum();

    return new DashboardMetricsResponse(
        from,
        to,
        new DashboardMetricsResponse.QcRejectedRate(
            qcRejected,
            qcPassed,
            qcRejected + qcPassed,
            percentage(qcRejected, qcRejected + qcPassed)),
        new DashboardMetricsResponse.AverageDuration(
            manufacturingDurations.size(),
            average(manufacturingDurations)),
        new DashboardMetricsResponse.ManualInterventions(
            openInterventions.size(),
            manualCompleted,
            openInterventions),
        new DashboardMetricsResponse.ManufacturingFailureRate(
            manufacturingFailed,
            manufacturingCompleted,
            manufacturingFailed + manufacturingCompleted,
            percentage(manufacturingFailed, manufacturingFailed + manufacturingCompleted),
            events.stream()
                .filter(event -> DashboardEventNormalizer.MANUFACTURING_FAILED.equals(event.eventType()))
                .map(DashboardEvent::itemIdentifier)
                .distinct()
                .count()),
        durationStats(endToEndDurations),
        wipByStage,
        new DashboardMetricsResponse.RetryRate(
            completedItems,
            totalRetries,
            completedItems == 0 ? 0.0 : (double) totalRetries / completedItems,
            new LinkedHashMap<>(retriesPerItem)));
  }

  private long count(List<DashboardEvent> events, String eventType) {
    return events.stream().filter(event -> eventType.equals(event.eventType())).count();
  }

  private double percentage(long numerator, long denominator) {
    return denominator == 0 ? 0.0 : ((double) numerator / denominator) * 100.0;
  }

  private double average(List<Long> values) {
    return values.isEmpty() ? 0.0 : values.stream().mapToLong(Long::longValue).average().orElse(0.0);
  }

  private DashboardMetricsResponse.DurationStats durationStats(List<Long> values) {
    if (values.isEmpty()) {
      return new DashboardMetricsResponse.DurationStats(0, 0.0, 0, 0);
    }
    return new DashboardMetricsResponse.DurationStats(
        values.size(),
        average(values),
        values.stream().mapToLong(Long::longValue).min().orElse(0),
        values.stream().mapToLong(Long::longValue).max().orElse(0));
  }

  private List<DashboardEvent> eventsInWindow(long fromMillis, long toMillis) {
    List<DashboardEvent> events = new ArrayList<>();
    ReadOnlyKeyValueStore<String, String> store = store(DashboardStateTransformer.DASHBOARD_EVENTS_STORE);
    try (KeyValueIterator<String, String> iterator = store.all()) {
      while (iterator.hasNext()) {
        String value = iterator.next().value;
        DashboardEvent event = objectMapper.readValue(value, DashboardEvent.class);
        if (event.timestamp() >= fromMillis && event.timestamp() <= toMillis) {
          events.add(event);
        }
      }
    } catch (Exception exception) {
      throw new IllegalStateException("Could not read dashboard event state", exception);
    }
    return events;
  }

  private List<ItemState> itemStates() {
    List<ItemState> states = new ArrayList<>();
    ReadOnlyKeyValueStore<String, String> store = store(DashboardStateTransformer.DASHBOARD_ITEMS_STORE);
    try (KeyValueIterator<String, String> iterator = store.all()) {
      while (iterator.hasNext()) {
        states.add(objectMapper.readValue(iterator.next().value, ItemState.class));
      }
    } catch (Exception exception) {
      throw new IllegalStateException("Could not read dashboard item state", exception);
    }
    return states;
  }

  private ReadOnlyKeyValueStore<String, String> store(String name) {
    KafkaStreams kafkaStreams = streamsBuilderFactoryBean.getKafkaStreams();
    if (kafkaStreams == null) {
      throw new InvalidStateStoreException("Kafka Streams is not running");
    }
    return kafkaStreams.store(StoreQueryParameters.fromNameAndType(
        name,
        QueryableStoreTypes.keyValueStore()));
  }
}
