package org.unisg.ftengrave.factoryeventstreams.streams;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RawFactoryEventTimestampExtractor implements TimestampExtractor {

  private static final Logger LOGGER =
      LoggerFactory.getLogger(RawFactoryEventTimestampExtractor.class);
  private static final DateTimeFormatter FACTORY_TIMESTAMP_FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS");

  private final ObjectMapper objectMapper;

  public RawFactoryEventTimestampExtractor(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  @Override
  public long extract(ConsumerRecord<Object, Object> record, long partitionTime) {
    if (!(record.value() instanceof String rawPayload)) {
      return fallbackTimestamp(record, partitionTime);
    }
    try {
      JsonNode timestampNode = objectMapper.readTree(rawPayload).get("timestamp");
      if (timestampNode != null && !timestampNode.isNull()) {
        return parseTimestamp(timestampNode.asText());
      }
    } catch (Exception exception) {
      LOGGER.warn("Could not extract event timestamp from raw factory event", exception);
    }
    return fallbackTimestamp(record, partitionTime);
  }

  private long fallbackTimestamp(ConsumerRecord<Object, Object> record, long partitionTime) {
    if (record.timestamp() >= 0) {
      return record.timestamp();
    }
    return Math.max(partitionTime, 0L);
  }

  private long parseTimestamp(String timestamp) {
    try {
      return Instant.parse(timestamp).toEpochMilli();
    } catch (Exception ignored) {
      return LocalDateTime.parse(timestamp, FACTORY_TIMESTAMP_FORMATTER)
          .atZone(ZoneId.systemDefault())
          .toInstant()
          .toEpochMilli();
    }
  }
}
