package org.unisg.ftengrave.factoryeventstreams.streams;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.OptionalLong;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.factoryeventstreams.dto.ItemStationAssignment;

@Component
public class RawFactoryEventEnricher {

  static final String SOURCE_TOPIC_FIELD = "_sourceTopic";
  static final String ITEM_IDENTIFIER_FIELD = "itemIdentifier";
  static final String ORCHESTRATION_STATION_FIELD = "orchestrationStation";
  static final String ITEM_STATION_VALID_FROM_FIELD = "itemStationValidFromTimestamp";
  static final String UNKNOWN_ITEM_IDENTIFIER = "UNKNOWN";

  private static final Logger LOGGER = LoggerFactory.getLogger(RawFactoryEventEnricher.class);
  private static final DateTimeFormatter FACTORY_TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS");

  private final ObjectMapper objectMapper;
  private final StageCommandStationMapper stationMapper;

  public RawFactoryEventEnricher(ObjectMapper objectMapper, StageCommandStationMapper stationMapper) {
    this.objectMapper = objectMapper;
    this.stationMapper = stationMapper;
  }

  public String attachSourceTopic(String sourceTopic, String rawPayload) {
    try {
      ObjectNode root = objectMapper.readTree(rawPayload).deepCopy();
      root.put(SOURCE_TOPIC_FIELD, sourceTopic);
      return objectMapper.writeValueAsString(root);
    } catch (Exception exception) {
      LOGGER.warn("Could not attach source topic to raw factory event", exception);
      return rawPayload;
    }
  }

  public String stationKey(String sourceTopic, String rawPayload) {
    try {
      JsonNode root = objectMapper.readTree(rawPayload);
      return stationMapper.stationForRawEvent(sourceTopic, root).value();
    } catch (Exception exception) {
      LOGGER.warn("Could not resolve orchestration station for raw factory event", exception);
      return OrchestrationStation.UNKNOWN.value();
    }
  }

  public String enrich(String rawPayload, String assignmentJson) {
    try {
      ObjectNode root = objectMapper.readTree(rawPayload).deepCopy();
      OrchestrationStation sensorStation = stationMapper.stationForRawEvent(null, root);
      ItemStationAssignment assignment = parseAssignment(assignmentJson);
      OptionalLong rawEventTimestamp = eventTimestamp(root);
      boolean assignmentValid = assignment != null
          && sensorStation.value().equals(assignment.station())
          && rawEventTimestamp.isPresent()
          && rawEventTimestamp.getAsLong() >= assignment.validFromTimestamp();

      if (assignmentValid) {
        root.put(ITEM_IDENTIFIER_FIELD, assignment.itemIdentifier());
        root.put(ORCHESTRATION_STATION_FIELD, assignment.station());
        root.put(ITEM_STATION_VALID_FROM_FIELD, assignment.validFromTimestamp());
      } else {
        root.put(ITEM_IDENTIFIER_FIELD, UNKNOWN_ITEM_IDENTIFIER);
        root.put(ORCHESTRATION_STATION_FIELD, OrchestrationStation.UNKNOWN.value());
        root.remove(ITEM_STATION_VALID_FROM_FIELD);
      }
      return objectMapper.writeValueAsString(root);
    } catch (Exception exception) {
      LOGGER.warn("Could not enrich raw factory event with item context", exception);
      return rawPayload;
    }
  }

  private ItemStationAssignment parseAssignment(String assignmentJson) {
    if (assignmentJson == null || assignmentJson.isBlank()) {
      return null;
    }
    try {
      return objectMapper.readValue(assignmentJson, ItemStationAssignment.class);
    } catch (Exception exception) {
      LOGGER.warn("Ignoring malformed item station assignment from table", exception);
      return null;
    }
  }

  private OptionalLong eventTimestamp(JsonNode root) {
    if (!root.hasNonNull("timestamp")) {
      return OptionalLong.empty();
    }
    String timestamp = root.get("timestamp").asText();
    try {
      return OptionalLong.of(Instant.parse(timestamp).toEpochMilli());
    } catch (Exception ignored) {
      try {
        return OptionalLong.of(LocalDateTime.parse(timestamp, FACTORY_TIMESTAMP_FORMATTER)
            .atZone(ZoneId.systemDefault())
            .toInstant()
            .toEpochMilli());
      } catch (Exception exception) {
        LOGGER.warn("Could not parse raw factory event timestamp {}", timestamp, exception);
        return OptionalLong.empty();
      }
    }
  }
}
