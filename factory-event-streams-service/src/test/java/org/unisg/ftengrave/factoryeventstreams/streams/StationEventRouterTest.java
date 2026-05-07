package org.unisg.ftengrave.factoryeventstreams.streams;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factoryeventstreams.dto.SensorLevelEvent;
import org.unisg.ftengrave.factoryeventstreams.dto.TranslatedMachineEvent;
import org.unisg.ftengrave.factoryeventstreams.translation.StationSensorTranslator;

class StationEventRouterTest {

  @Test
  void routesToFirstTranslatorThatSupportsStationEvent() {
    StationSensorTranslator translator = new StationSensorTranslator() {
      @Override
      public boolean supports(SensorLevelEvent event) {
        return "SM_1".equals(event.getStation());
      }

      @Override
      public List<TranslatedMachineEvent> translate(SensorLevelEvent event) {
        return List.of(new TranslatedMachineEvent("sorting-machine-events", "event", "{}"));
      }
    };
    StationEventRouter router = new StationEventRouter(List.of(translator));

    List<TranslatedMachineEvent> translatedEvents = router.route(event("SM_1"));

    assertEquals(1, translatedEvents.size());
    assertEquals("sorting-machine-events", translatedEvents.getFirst().topic());
  }

  @Test
  void ignoresEventsWithoutRegisteredTranslator() {
    StationEventRouter router = new StationEventRouter(List.of());

    assertTrue(router.route(event("OV_1")).isEmpty());
  }

  private SensorLevelEvent event(String station) {
    return new SensorLevelEvent(
        "evt-1", "FTFactory/SM_1", station, "2026-04-02T10:15:30Z",
        "i3_light_barrier", "0", Map.of());
  }
}
