package org.unisg.ftengrave.factoryeventstreams.translation.workstation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factoryeventstreams.dto.SensorLevelEvent;

class WorkstationTransportSensorTranslatorTest {

  private final WorkstationTransportSensorTranslator translator =
      new WorkstationTransportSensorTranslator(
          new ObjectMapper(), "FTFactory/WT_1,WT_1", "workstation-transport-events");

  @Test
  void translatesRisingEdgeOfReadyAndIdleState() {
    assertTrue(translator.translate(event("busy", "pick_up_and_transport")).isEmpty());

    assertEquals(
        "{\"eventType\":\"wt-move-completed\",\"itemIdentifier\":\"item-42\"}",
        translator.translate(event("ready", "")).getFirst().payloadJson());
    assertTrue(translator.translate(event("ready", "")).isEmpty());
  }

  private SensorLevelEvent event(String currentState, String currentTask) {
    return new SensorLevelEvent("evt-1", "FTFactory/WT_1", "WT_1",
        "2026-04-02T10:15:30Z", "current_state", currentState, "item-42",
        "Manufacturing", Map.of("currentTask", currentTask));
  }
}
