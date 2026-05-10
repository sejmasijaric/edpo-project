package org.unisg.ftengrave.factoryeventstreams.streams;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.apache.kafka.streams.KeyValue;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factoryeventstreams.dto.SensorLevelEvent;

class RawFactoryEventSplitterTest {

  private final RawFactoryEventSplitter splitter = new RawFactoryEventSplitter(new ObjectMapper());

  @Test
  void splitsRawFactoryEventIntoSensorLevelEvents() {
    List<KeyValue<String, SensorLevelEvent>> events = splitter.split("FTFactory/SM_1", """
        {
          "id":"evt-1",
          "station":"SM_1",
          "timestamp":"2026-04-02T10:15:30Z",
          "i1_light_barrier":0,
          "i2_color_sensor":1350,
          "current_task":"detect_color"
        }
        """);

    assertEquals(2, events.size());
    SensorLevelEvent colorSensor = events.stream()
        .map(event -> event.value)
        .filter(event -> event.getSensorName().equals("i2_color_sensor"))
        .findFirst()
        .orElseThrow();
    assertEquals("evt-1", colorSensor.getOriginalEventId());
    assertEquals("SM_1", colorSensor.getStation());
    assertEquals("2026-04-02T10:15:30Z", colorSensor.getTimestamp());
    assertEquals("1350", colorSensor.getSensorValue());
    assertEquals("evt-1", colorSensor.getMetadata().get("eventId"));
    assertEquals("detect_color", colorSensor.getMetadata().get("currentTask"));
  }
}
