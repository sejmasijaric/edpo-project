package org.unisg.ftengrave.factoryeventstreams.streams;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.streams.KeyValue;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factoryeventstreams.dto.ItemStationAssignment;

class StageCommandStationMapperTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final StageCommandStationMapper mapper = new StageCommandStationMapper(
      objectMapper,
      "FTFactory/VGR_1,VGR_1",
      "FTFactory/OV_1,OV_1",
      "FTFactory/MM_1,MM_1",
      "FTFactory/WT_1,WT_1",
      "FTFactory/SM_1,SM_1");

  @Test
  void mapsOrderOrchestratorCommandsToStageAssignments() throws Exception {
    KeyValue<String, String> intake = mapper.toAssignment("""
        {"commandType":"run-item-intake-command","itemIdentifier":"item-1"}
        """, 1000L).orElseThrow();
    KeyValue<String, String> manufacturing = mapper.toAssignment("""
        {"commandType":"run-production-command","itemIdentifier":"item-2"}
        """, 2000L).orElseThrow();
    KeyValue<String, String> oc = mapper.toAssignment("""
        {"commandType":"run-item-qc-command","itemIdentifier":"item-3"}
        """, 3000L).orElseThrow();

    assertAssignment(intake, "Intake", "item-1", 1000L);
    assertAssignment(manufacturing, "Manufacturing", "item-2", 2000L);
    assertAssignment(oc, "OC", "item-3", 3000L);
  }

  private void assertAssignment(
      KeyValue<String, String> keyValue,
      String station,
      String itemIdentifier,
      long validFromTimestamp) throws Exception {
    ItemStationAssignment assignment =
        objectMapper.readValue(keyValue.value, ItemStationAssignment.class);

    assertEquals(station, keyValue.key);
    assertEquals(itemIdentifier, assignment.itemIdentifier());
    assertEquals(station, assignment.station());
    assertEquals(validFromTimestamp, assignment.validFromTimestamp());
  }
}
