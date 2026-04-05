package org.unisg.ftengrave.engraverintegrationservice.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.engraverintegrationservice.dto.EngraverEventDto;

class EngraverEventFilterTest {

  private final EngraverEventFilter filter = new EngraverEventFilter(new ObjectMapper());

  @Test
  void emitsArrivalAndReleaseEventsForEngraverSinkBarrier() {
    Optional<EngraverEventDto> arrivalEvent =
        filter.filter("FTFactory/OV_1", payload(0));

    Optional<EngraverEventDto> releaseEvent =
        filter.filter("FTFactory/OV_1", payload(1));

    assertTrue(arrivalEvent.isPresent());
    assertEquals("item-arrived-at-engraver-sink", arrivalEvent.get().getEventType());
    assertTrue(releaseEvent.isPresent());
    assertEquals("item-left-engraver-sink", releaseEvent.get().getEventType());
  }

  @Test
  void ignoresUnchangedSnapshots() {
    filter.filter("FTFactory/OV_1", payload(1));

    Optional<EngraverEventDto> event =
        filter.filter("FTFactory/OV_1", payload(1));

    assertTrue(event.isEmpty());
  }

  @Test
  void ignoresIncompletePayloads() {
    Optional<EngraverEventDto> event =
        filter.filter("FTFactory/OV_1", "{\"timestamp\":\"2026-04-03T10:15:30Z\"}");

    assertTrue(event.isEmpty());
  }

  private String payload(int i5LightBarrier) {
    return """
        {
          "timestamp":"2026-04-03T10:15:30Z",
          "i5_light_barrier":%d,
          "current_task":"idle",
          "current_task_duration":0.0
        }
        """.formatted(i5LightBarrier);
  }
}
