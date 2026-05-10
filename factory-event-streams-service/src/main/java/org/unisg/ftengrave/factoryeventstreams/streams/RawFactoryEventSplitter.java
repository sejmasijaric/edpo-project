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
      String station = station(root, sourceTopic);
      String timestamp = text(root, "timestamp");
      Map<String, String> metadata = metadata(root, sourceTopic);
      List<KeyValue<String, SensorLevelEvent>> sensorEvents = new ArrayList<>();

      Iterator<Map.Entry<String, JsonNode>> fields = root.fields();
      while (fields.hasNext()) {
        Map.Entry<String, JsonNode> field = fields.next();
        if (isMetadataField(field.getKey())) {
          continue;
        }
        SensorLevelEvent event = new SensorLevelEvent(
            originalEventId,
            sourceTopic,
            station,
            timestamp,
            field.getKey(),
            sensorValue(field.getValue()),
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
    return metadata;
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
        || "current_task_duration".equals(fieldName);
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
