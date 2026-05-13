package org.unisg.ftengrave.factoryeventstreams.translation.polishing;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factoryeventstreams.dto.SensorLevelEvent;

class PolishingMachineSensorTranslatorTest {

  private final PolishingMachineSensorTranslator translator =
      new PolishingMachineSensorTranslator(new ObjectMapper(), "FTFactory/MM_1,MM_1", "polishing-machine-events");

  @Test
  void translatesPolishingMachineOutputBarrierEvents() {
    assertEquals(
        "{\"eventType\":\"item-arrived-at-polishing-machine-output\",\"itemIdentifier\":\"item-42\"}",
        translator.translate(event("0")).getFirst().payloadJson());
    assertEquals(
        "{\"eventType\":\"item-left-polishing-machine-output\",\"itemIdentifier\":\"item-42\"}",
        translator.translate(event("1")).getFirst().payloadJson());
  }

  private SensorLevelEvent event(String sensorValue) {
    return new SensorLevelEvent("evt-1", "FTFactory/MM_1", "MM_1",
        "2026-04-02T10:15:30Z", "i4_light_barrier", sensorValue, "item-42",
        "Manufacturing", Map.of("currentTask", ""));
  }
}
