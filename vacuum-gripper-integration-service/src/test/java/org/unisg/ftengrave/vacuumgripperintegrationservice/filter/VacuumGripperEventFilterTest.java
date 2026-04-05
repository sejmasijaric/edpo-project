package org.unisg.ftengrave.vacuumgripperintegrationservice.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.vacuumgripperintegrationservice.dto.VacuumGripperEventDto;

class VacuumGripperEventFilterTest {

  private final VacuumGripperEventFilter filter = new VacuumGripperEventFilter(new ObjectMapper());

  @Test
  void emitsArrivalAndReleaseEventsForInputBarrier() {
    Optional<VacuumGripperEventDto> arrivalEvent =
        filter.filter("FTFactory/VGR_1", payload(0, 1));

    Optional<VacuumGripperEventDto> releaseEvent =
        filter.filter("FTFactory/VGR_1", payload(1, 1));

    assertTrue(arrivalEvent.isPresent());
    assertEquals("item-arrived-at-input", arrivalEvent.get().getEventType());
    assertTrue(releaseEvent.isPresent());
    assertEquals("item-left-input", releaseEvent.get().getEventType());
  }

  @Test
  void emitsArrivalAndReleaseEventsForOutputBarrier() {
    filter.filter("FTFactory/VGR_1", payload(1, 1));

    Optional<VacuumGripperEventDto> arrivalEvent =
        filter.filter("FTFactory/VGR_1", payload(1, 0));

    Optional<VacuumGripperEventDto> releaseEvent =
        filter.filter("FTFactory/VGR_1", payload(1, 1));

    assertTrue(arrivalEvent.isPresent());
    assertEquals("item-arrived-at-output", arrivalEvent.get().getEventType());
    assertTrue(releaseEvent.isPresent());
    assertEquals("item-left-output", releaseEvent.get().getEventType());
  }

  @Test
  void ignoresUnchangedSnapshots() {
    filter.filter("FTFactory/VGR_1", payload(1, 1));

    Optional<VacuumGripperEventDto> event =
        filter.filter("FTFactory/VGR_1", payload(1, 1));

    assertTrue(event.isEmpty());
  }

  @Test
  void ignoresIncompletePayloads() {
    Optional<VacuumGripperEventDto> event =
        filter.filter("FTFactory/VGR_1", "{\"timestamp\":\"2026-04-03T10:15:30Z\"}");

    assertTrue(event.isEmpty());
  }

  private String payload(int i7LightBarrier, int i4LightBarrier) {
    return """
        {
          "timestamp":"2026-04-03T10:15:30Z",
          "i7_light_barrier":%d,
          "i4_light_barrier":%d,
          "current_task":"idle",
          "current_task_duration":0.0
        }
        """.formatted(i7LightBarrier, i4LightBarrier);
  }
}
