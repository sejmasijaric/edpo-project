package org.unisg.ftengrave.factoryeventstreams.streams;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.apache.kafka.streams.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.factoryeventstreams.dto.SensorLevelEvent;

@Component
public class RawFactoryEventSplitter {

  private static final Logger LOGGER = LoggerFactory.getLogger(RawFactoryEventSplitter.class);

  private final ObjectMapper objectMapper;

  public RawFactoryEventSplitter(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public List<KeyValue<String, SensorLevelEvent>> split(String sourceTopic, String rawPayload) {
    try {
      JsonNode root = objectMapper.readTree(rawPayload);
      String originalEventId = text(root, "id");
      String originalSourceTopic = originalSourceTopic(root, sourceTopic);
      String station = station(root, originalSourceTopic);
      String timestamp = text(root, "timestamp");
      String itemIdentifier = text(root, RawFactoryEventEnricher.ITEM_IDENTIFIER_FIELD);
      String orchestrationStation = text(root, RawFactoryEventEnricher.ORCHESTRATION_STATION_FIELD);
      Map<String, String> metadata = metadata(root, originalSourceTopic);
      List<KeyValue<String, SensorLevelEvent>> sensorEvents = new ArrayList<>();

      Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();
        if (isMetadataField(field.getKey())) {
          continue;
        }
        SensorLevelEvent event = new SensorLevelEvent(
            originalEventId,
            originalSourceTopic,
            station,
            timestamp,
            field.getKey(),
            sensorValue(field.getValue()),
            itemIdentifier,
            orchestrationStation,
            metadata);
        sensorEvents.add(KeyValue.pair(event.sensorKey(), event));
      }

      LOGGER.debug("Split raw factory event {} from station {} into {} sensor events",
          originalEventId, station, sensorEvents.size());
      return sensorEvents;
    } catch (Exception exception) {
      LOGGER.warn("Ignoring malformed raw factory event from MQTT topic {}", sourceTopic, exception);
      return List.of();
    }
  }

  private Map<String, String> metadata(JsonNode root, String sourceTopic) {
    Map<String, String> metadata = new LinkedHashMap<>();
    metadata.put("sourceTopic", sourceTopic);
    if (root.hasNonNull("id")) {
      metadata.put("eventId", root.get("id").asText());
    }
    metadata.put("currentTask", root.hasNonNull("current_task") ? root.get("current_task").asText() : "");
    if (root.hasNonNull("current_task_duration")) {
      metadata.put("currentTaskDuration", root.get("current_task_duration").asText());
    }
    if (root.hasNonNull(RawFactoryEventEnricher.ITEM_IDENTIFIER_FIELD)) {
      metadata.put("itemIdentifier", root.get(RawFactoryEventEnricher.ITEM_IDENTIFIER_FIELD).asText());
    }
    if (root.hasNonNull(RawFactoryEventEnricher.ORCHESTRATION_STATION_FIELD)) {
      metadata.put("orchestrationStation",
          root.get(RawFactoryEventEnricher.ORCHESTRATION_STATION_FIELD).asText());
    }
    if (root.hasNonNull(RawFactoryEventEnricher.ITEM_STATION_VALID_FROM_FIELD)) {
      metadata.put("itemStationValidFromTimestamp",
          root.get(RawFactoryEventEnricher.ITEM_STATION_VALID_FROM_FIELD).asText());
    }
    return metadata;
  }

  private String originalSourceTopic(JsonNode root, String sourceTopic) {
    String originalSourceTopic = text(root, RawFactoryEventEnricher.SOURCE_TOPIC_FIELD);
    return originalSourceTopic == null || originalSourceTopic.isBlank() ? sourceTopic : originalSourceTopic;
  }

  private String station(JsonNode root, String sourceTopic) {
    String station = text(root, "station");
    return station == null || station.isBlank() ? sourceTopic : station;
  }

  private String text(JsonNode root, String field) {
    return root.hasNonNull(field) ? root.get(field).asText() : null;
  }

  private boolean isMetadataField(String fieldName) {
    return "id".equals(fieldName)
        || "station".equals(fieldName)
        || "timestamp".equals(fieldName)
        || "current_task".equals(fieldName)
        || "current_task_duration".equals(fieldName)
        || RawFactoryEventEnricher.SOURCE_TOPIC_FIELD.equals(fieldName)
        || RawFactoryEventEnricher.ITEM_IDENTIFIER_FIELD.equals(fieldName)
        || RawFactoryEventEnricher.ORCHESTRATION_STATION_FIELD.equals(fieldName)
        || RawFactoryEventEnricher.ITEM_STATION_VALID_FROM_FIELD.equals(fieldName);
  }

  private String sensorValue(JsonNode value) {
    if (value == null || value.isNull()) {
      return null;
    }
    if (value.isValueNode()) {
      return value.asText();
    }
    return value.toString();
  }
}
