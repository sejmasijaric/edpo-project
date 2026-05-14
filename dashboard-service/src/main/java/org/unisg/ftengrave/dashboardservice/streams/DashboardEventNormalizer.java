package org.unisg.ftengrave.dashboardservice.streams;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import org.unisg.ftengrave.dashboardservice.dto.DashboardEvent;

public class DashboardEventNormalizer {

  public static final String RUN_ITEM_INTAKE_COMMAND = "run-item-intake-command";
  public static final String RUN_PRODUCTION_COMMAND = "run-production-command";
  public static final String RUN_ITEM_QC_COMMAND = "run-item-qc-command";
  public static final String MANUFACTURING_COMPLETED = "manufacturing-completed";
  public static final String MANUFACTURING_FAILED = "manufacturing-failed";
  public static final String QC_SHIPPING = "qc-shipping";
  public static final String QC_REJECTION = "qc-rejection";
  public static final String ORDER_CREATED = "order-created";
  public static final String MANUAL_INTERVENTION_ISSUED = "manual-intervention-issued";
  public static final String MANUAL_INTERVENTION_COMPLETED = "manual-intervention-completed";
  public static final String MANUFACTURING_ATTEMPT_DURATION = "manufacturing-attempt-duration";

  private final ObjectMapper objectMapper;

  public DashboardEventNormalizer(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public Optional<DashboardEvent> normalize(String sourceTopic, String key, String payload, long timestamp) {
    if (payload == null || payload.isBlank()) {
      return Optional.empty();
    }
    try {
      JsonNode node = objectMapper.readTree(payload);
      String itemIdentifier = text(node, "itemIdentifier").orElse(key);
      if (itemIdentifier == null || itemIdentifier.isBlank()) {
        return Optional.empty();
      }

      String eventType = text(node, "outcomeType")
          .or(() -> text(node, "commandType"))
          .or(() -> text(node, "eventType"))
          .orElseGet(() -> sourceTopic.equals("order-created") ? ORDER_CREATED : null);
      String taskName = text(node, "taskName").orElse(null);
      String taskStatus = text(node, "taskStatus")
          .or(() -> text(node, "status"))
          .or(() -> text(node, "state"))
          .orElse(null);

      if (taskName != null && !taskName.isBlank()) {
        eventType = isCompletedTask(taskStatus, eventType) ? MANUAL_INTERVENTION_COMPLETED : MANUAL_INTERVENTION_ISSUED;
      }
      if (eventType == null || eventType.isBlank()) {
        return Optional.empty();
      }

      return Optional.of(new DashboardEvent(
          stableEventId(sourceTopic, key, payload, timestamp),
          itemIdentifier,
          eventType,
          sourceTopic,
          timestamp,
          taskName,
          taskStatus,
          stageFor(eventType),
          null,
          attributes(node)));
    } catch (Exception ignored) {
      return Optional.empty();
    }
  }

  public DashboardEvent manufacturingAttemptDuration(
      String sourceTopic,
      String itemIdentifier,
      String outcomeType,
      long startTimestamp,
      long endTimestamp,
      long durationMillis) {
    return new DashboardEvent(
        stableEventId(sourceTopic, itemIdentifier, outcomeType + startTimestamp + endTimestamp, endTimestamp),
        itemIdentifier,
        MANUFACTURING_ATTEMPT_DURATION,
        sourceTopic,
        endTimestamp,
        null,
        null,
        "MANUFACTURING",
        durationMillis,
        Map.of(
            "outcomeType", outcomeType,
            "startTimestamp", Long.toString(startTimestamp),
            "endTimestamp", Long.toString(endTimestamp)));
  }

  private boolean isCompletedTask(String taskStatus, String eventType) {
    return containsCompleted(taskStatus) || containsCompleted(eventType);
  }

  private boolean containsCompleted(String value) {
    if (value == null) {
      return false;
    }
    String normalized = value.toLowerCase();
    return normalized.contains("complete") || normalized.contains("resolved") || normalized.contains("closed");
  }

  private Optional<String> text(JsonNode node, String fieldName) {
    JsonNode value = node.get(fieldName);
    if (value == null || value.isNull()) {
      return Optional.empty();
    }
    String text = value.asText();
    return text == null || text.isBlank() ? Optional.empty() : Optional.of(text);
  }

  private String stageFor(String eventType) {
    return switch (eventType) {
      case RUN_ITEM_INTAKE_COMMAND -> "INTAKE";
      case RUN_PRODUCTION_COMMAND, MANUFACTURING_COMPLETED, MANUFACTURING_FAILED -> "MANUFACTURING";
      case RUN_ITEM_QC_COMMAND, QC_SHIPPING, QC_REJECTION -> "QC";
      case MANUAL_INTERVENTION_ISSUED, MANUAL_INTERVENTION_COMPLETED -> "MANUAL_INTERVENTION";
      default -> null;
    };
  }

  private Map<String, String> attributes(JsonNode node) {
    Map<String, String> attributes = new LinkedHashMap<>();
    node.fields().forEachRemaining(entry -> {
      if (entry.getValue() != null && !entry.getValue().isNull()) {
        attributes.put(entry.getKey(), entry.getValue().asText());
      }
    });
    return attributes;
  }

  private String stableEventId(String sourceTopic, String key, String payload, long timestamp) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.update(sourceTopic.getBytes(StandardCharsets.UTF_8));
      digest.update((byte) 0);
      digest.update(String.valueOf(key).getBytes(StandardCharsets.UTF_8));
      digest.update((byte) 0);
      digest.update(payload.getBytes(StandardCharsets.UTF_8));
      digest.update((byte) 0);
      digest.update(Long.toString(timestamp).getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest.digest());
    } catch (Exception exception) {
      return sourceTopic + "-" + timestamp + "-" + Math.abs(payload.hashCode());
    }
  }
}
