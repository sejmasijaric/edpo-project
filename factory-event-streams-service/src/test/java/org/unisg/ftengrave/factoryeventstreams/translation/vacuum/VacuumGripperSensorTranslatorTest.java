package org.unisg.ftengrave.factoryeventstreams.translation.vacuum;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factoryeventstreams.dto.SensorLevelEvent;
import org.unisg.ftengrave.factoryeventstreams.dto.TranslatedMachineEvent;

class VacuumGripperSensorTranslatorTest {

  private final VacuumGripperSensorTranslator translator =
      new VacuumGripperSensorTranslator(new ObjectMapper(), "FTFactory/VGR_1,VGR_1", "vacuum-gripper-events");

  @Test
  void translatesInputAndOutputBarrierEvents() {
    List<TranslatedMachineEvent> inputArrival = translator.translate(event("i7_light_barrier", "0"));
    List<TranslatedMachineEvent> inputRelease = translator.translate(event("i7_light_barrier", "1"));
    List<TranslatedMachineEvent> outputArrival = translator.translate(event("i4_light_barrier", "0"));

    assertEquals("{\"eventType\":\"item-arrived-at-intake\"}", inputArrival.getFirst().payloadJson());
    assertEquals("{\"eventType\":\"item-left-intake\"}", inputRelease.getFirst().payloadJson());
    assertEquals("{\"eventType\":\"item-arrived-at-output\"}", outputArrival.getFirst().payloadJson());
  }

  @Test
  void ignoresUnsupportedSensor() {
    assertTrue(translator.translate(event("current_state", "ready")).isEmpty());
  }

  private SensorLevelEvent event(String sensorName, String sensorValue) {
    return new SensorLevelEvent("evt-1", "FTFactory/VGR_1", "VGR_1",
        "2026-04-02T10:15:30Z", sensorName, sensorValue, Map.of("currentTask", ""));
  }
}
