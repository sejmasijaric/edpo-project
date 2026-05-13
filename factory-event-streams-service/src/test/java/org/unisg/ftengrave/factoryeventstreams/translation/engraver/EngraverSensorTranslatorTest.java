package org.unisg.ftengrave.factoryeventstreams.translation.engraver;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factoryeventstreams.dto.SensorLevelEvent;

class EngraverSensorTranslatorTest {

  private final EngraverSensorTranslator translator =
      new EngraverSensorTranslator(new ObjectMapper(), "FTFactory/OV_1,OV_1", "engraver-events");

  @Test
  void translatesEngraverSinkBarrierEvents() {
    assertEquals(
        "{\"eventType\":\"item-arrived-at-engraver-sink\",\"itemIdentifier\":\"item-42\"}",
        translator.translate(event("0")).getFirst().payloadJson());
    assertEquals(
        "{\"eventType\":\"item-left-engraver-sink\",\"itemIdentifier\":\"item-42\"}",
        translator.translate(event("1")).getFirst().payloadJson());
  }

  private SensorLevelEvent event(String sensorValue) {
    return new SensorLevelEvent("evt-1", "FTFactory/OV_1", "OV_1",
        "2026-04-02T10:15:30Z", "i5_light_barrier", sensorValue, "item-42",
        "Manufacturing", Map.of("currentTask", ""));
  }
}
