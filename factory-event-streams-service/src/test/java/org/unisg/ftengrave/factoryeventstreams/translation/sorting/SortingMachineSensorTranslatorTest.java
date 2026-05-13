package org.unisg.ftengrave.factoryeventstreams.translation.sorting;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factoryeventstreams.dto.SensorLevelEvent;
import org.unisg.ftengrave.factoryeventstreams.dto.TranslatedMachineEvent;

class SortingMachineSensorTranslatorTest {

  private final SortingMachineSensorTranslator translator =
      new SortingMachineSensorTranslator(new ObjectMapper(), "FTFactory/SM_1,SM_1", "sorting-machine-events");

  @Test
  void supportsConfiguredSortingMachineStations() {
    assertTrue(translator.supports(event("i3_light_barrier", "0")));
  }

  @Test
  void translatesSortingMachineColorSensorToBusinessEvent() {
    List<TranslatedMachineEvent> events = translator.translate(event("i2_color_sensor", "1350"));

    assertEquals(1, events.size());
    assertEquals("sorting-machine-events", events.getFirst().topic());
    assertEquals("color-detected", events.getFirst().key());
    assertEquals(
        "{\"eventType\":\"color-detected\",\"color\":\"red\",\"itemIdentifier\":\"item-42\"}",
        events.getFirst().payloadJson());
  }

  @Test
  void doesNotEmitRepeatedColorDetectionUntilSensorResets() {
    translator.translate(event("i2_color_sensor", "1350"));
    assertTrue(translator.translate(event("i2_color_sensor", "950")).isEmpty());

    translator.translate(event("i2_color_sensor", "1800"));
    List<TranslatedMachineEvent> events = translator.translate(event("i2_color_sensor", "950"));

    assertEquals(
        "{\"eventType\":\"color-detected\",\"color\":\"white\",\"itemIdentifier\":\"item-42\"}",
        events.getFirst().payloadJson());
  }

  @Test
  void translatesSortingMachineQcBarrierToBusinessEvents() {
    List<TranslatedMachineEvent> firstIdleObservation = translator.translate(event("i3_light_barrier", "1"));
    List<TranslatedMachineEvent> arrival = translator.translate(event("i3_light_barrier", "0"));
    List<TranslatedMachineEvent> release = translator.translate(event("i3_light_barrier", "1"));

    assertTrue(firstIdleObservation.isEmpty());
    assertEquals(
        "{\"eventType\":\"item-arrived-at-qc\",\"color\":null,\"itemIdentifier\":\"item-42\"}",
        arrival.getFirst().payloadJson());
    assertEquals(
        "{\"eventType\":\"item-left-qc\",\"color\":null,\"itemIdentifier\":\"item-42\"}",
        release.getFirst().payloadJson());
  }

  private SensorLevelEvent event(String sensorName, String sensorValue) {
    return new SensorLevelEvent(
        "evt-1", "FTFactory/SM_1", "SM_1", "2026-04-02T10:15:30Z",
        sensorName, sensorValue, "item-42", "OC", Map.of());
  }
}
