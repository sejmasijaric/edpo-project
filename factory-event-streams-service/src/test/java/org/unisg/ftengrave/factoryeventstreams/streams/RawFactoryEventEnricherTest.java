package org.unisg.ftengrave.factoryeventstreams.streams;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factoryeventstreams.dto.ItemStationAssignment;

class RawFactoryEventEnricherTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final StageCommandStationMapper stationMapper = new StageCommandStationMapper(
      objectMapper,
      "FTFactory/VGR_1,VGR_1",
      "FTFactory/OV_1,OV_1",
      "FTFactory/MM_1,MM_1",
      "FTFactory/WT_1,WT_1",
      "FTFactory/SM_1,SM_1");
  private final RawFactoryEventEnricher enricher =
      new RawFactoryEventEnricher(objectMapper, stationMapper);

  @Test
  void enrichesRawEventWhenStationAssignmentIsValidForEventTimestamp() throws Exception {
    long validFrom = Instant.parse("2026-04-02T10:15:00Z").toEpochMilli();
    String assignment = objectMapper.writeValueAsString(
        new ItemStationAssignment("item-42", "OC", validFrom));

    JsonNode enriched = objectMapper.readTree(enricher.enrich("""
        {
          "_sourceTopic":"FTFactory/SM_1",
          "station":"SM_1",
          "timestamp":"2026-04-02T10:15:30Z",
          "i2_color_sensor":1350
        }
        """, assignment));

    assertEquals("item-42", enriched.get("itemIdentifier").asText());
    assertEquals("OC", enriched.get("orchestrationStation").asText());
    assertEquals(validFrom, enriched.get("itemStationValidFromTimestamp").asLong());
  }

  @Test
  void enrichesRawEventUsingSourceTopicWhenStationFieldIsMissing() throws Exception {
    long validFrom = Instant.parse("2026-05-13T12:31:33.826Z").toEpochMilli();
    String assignment = objectMapper.writeValueAsString(
        new ItemStationAssignment(
            "b5163f29-cdf8-4003-98d1-983b5f5ac924", "Intake", validFrom));

    JsonNode enriched = objectMapper.readTree(enricher.enrich("""
        {
          "timestamp":"2026-05-13 14:46:32.40",
          "i7_light_barrier":1,
          "i4_light_barrier":1,
          "current_task":"",
          "current_task_duration":0.0,
          "_sourceTopic":"FTFactory/VGR_1"
        }
        """, assignment));

    assertEquals(
        "b5163f29-cdf8-4003-98d1-983b5f5ac924",
        enriched.get("itemIdentifier").asText());
    assertEquals("Intake", enriched.get("orchestrationStation").asText());
    assertEquals(validFrom, enriched.get("itemStationValidFromTimestamp").asLong());
  }

  @Test
  void usesUnknownWhenRawEventPredatesLatestStationAssignment() throws Exception {
    long validFrom = Instant.parse("2026-04-02T10:15:00Z").toEpochMilli();
    String assignment = objectMapper.writeValueAsString(
        new ItemStationAssignment("item-42", "OC", validFrom));

    JsonNode enriched = objectMapper.readTree(enricher.enrich("""
        {
          "_sourceTopic":"FTFactory/SM_1",
          "station":"SM_1",
          "timestamp":"2026-04-02T10:14:59Z",
          "i2_color_sensor":1350
        }
        """, assignment));

    assertEquals("UNKNOWN", enriched.get("itemIdentifier").asText());
    assertEquals("UNKNOWN", enriched.get("orchestrationStation").asText());
  }

  @Test
  void usesUnknownWhenSensorStationDoesNotMatchAssignmentStation() throws Exception {
    long validFrom = Instant.parse("2026-04-02T10:15:00Z").toEpochMilli();
    String assignment = objectMapper.writeValueAsString(
        new ItemStationAssignment("item-42", "Intake", validFrom));

    JsonNode enriched = objectMapper.readTree(enricher.enrich("""
        {
          "_sourceTopic":"FTFactory/SM_1",
          "station":"SM_1",
          "timestamp":"2026-04-02T10:15:30Z",
          "i2_color_sensor":1350
        }
        """, assignment));

    assertEquals("UNKNOWN", enriched.get("itemIdentifier").asText());
    assertEquals("UNKNOWN", enriched.get("orchestrationStation").asText());
  }

  @Test
  void usesUnknownWhenRawEventTimestampIsMissing() throws Exception {
    long validFrom = Instant.parse("2026-04-02T10:15:00Z").toEpochMilli();
    String assignment = objectMapper.writeValueAsString(
        new ItemStationAssignment("item-42", "OC", validFrom));

    JsonNode enriched = objectMapper.readTree(enricher.enrich("""
        {
          "_sourceTopic":"FTFactory/SM_1",
          "station":"SM_1",
          "i2_color_sensor":1350
        }
        """, assignment));

    assertEquals("UNKNOWN", enriched.get("itemIdentifier").asText());
    assertEquals("UNKNOWN", enriched.get("orchestrationStation").asText());
  }
}
