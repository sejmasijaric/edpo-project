package org.unisg.ftengrave.polishingmachineintegrationservice.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.polishingmachineintegrationservice.dto.PolishingMachineEventDto;

class PolishingMachineEventFilterTest {

  private final PolishingMachineEventFilter filter =
      new PolishingMachineEventFilter(new ObjectMapper());

  @Test
  void emitsArrivalAndReleaseEventsForPolishingMachineOutputBarrier() {
    Optional<PolishingMachineEventDto> arrivalEvent =
        filter.filter("FTFactory/MM_1", payload(0));

    Optional<PolishingMachineEventDto> releaseEvent =
        filter.filter("FTFactory/MM_1", payload(1));

    assertTrue(arrivalEvent.isPresent());
    assertEquals("item-arrived-at-polishing-machine-output", arrivalEvent.get().getEventType());
    assertTrue(releaseEvent.isPresent());
    assertEquals("item-left-polishing-machine-output", releaseEvent.get().getEventType());
  }

  @Test
  void ignoresUnchangedSnapshots() {
    filter.filter("FTFactory/MM_1", payload(1));

    Optional<PolishingMachineEventDto> event =
        filter.filter("FTFactory/MM_1", payload(1));

    assertTrue(event.isEmpty());
  }

  @Test
  void ignoresIncompletePayloads() {
    Optional<PolishingMachineEventDto> event =
        filter.filter("FTFactory/MM_1", "{\"timestamp\":\"2026-04-03T10:15:30Z\"}");

    assertTrue(event.isEmpty());
  }

  private String payload(int i4LightBarrier) {
    return """
        {
          "timestamp":"2026-04-03T10:15:30Z",
          "i4_light_barrier":%d,
          "current_task":"idle",
          "current_task_duration":0.0
        }
        """.formatted(i4LightBarrier);
  }
}
