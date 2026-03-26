package org.unisg.ftengrave.sorterintegrationsetrvice.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.sorterintegrationsetrvice.dto.SortingMachineEventDto;

class SortingMachineEventFilterTest {

  private final SortingMachineEventFilter filter = new SortingMachineEventFilter(new ObjectMapper());

  @Test
  void filterEmitsDetectedColorEventWhenColorSensorSeesWorkpiece() {
    Optional<SortingMachineEventDto> event =
        filter.filter("FTFactory/SM_1", payload(1300, 0, 1, 1, 1));

    assertTrue(event.isPresent());
    assertEquals("detected-color-red", event.get().getEventType());
  }

  @Test
  void filterEmitsRejectSortEventWhenRejectBarrierIsZero() {
    filter.filter("FTFactory/SM_1", payload(1300, 0, 1, 1, 1));

    Optional<SortingMachineEventDto> event =
        filter.filter("FTFactory/SM_1", payload(1300, 1, 0, 1, 1));

    assertTrue(event.isPresent());
    assertEquals("sort_to_reject", event.get().getEventType());
  }

  @Test
  void filterEmitsShippingSortEventWhenShippingBarrierIsZero() {
    Optional<SortingMachineEventDto> event =
        filter.filter("FTFactory/SM_1", payload(1600, 1, 1, 0, 1));

    assertTrue(event.isPresent());
    assertEquals("sort_to_shipping", event.get().getEventType());
  }

  @Test
  void filterEmitsRetrySortEventWhenRetryBarrierIsZero() {
    Optional<SortingMachineEventDto> event =
        filter.filter("FTFactory/SM_1", payload(800, 1, 1, 1, 0));

    assertTrue(event.isPresent());
    assertEquals("sort_to_retry", event.get().getEventType());
  }

  @Test
  void filterSuppressesDuplicateEventsWhileStateDoesNotChange() {
    filter.filter("FTFactory/SM_1", payload(1300, 0, 1, 1, 1));

    Optional<SortingMachineEventDto> event =
        filter.filter("FTFactory/SM_1", payload(1300, 0, 1, 1, 1));

    assertFalse(event.isPresent());
  }

  private String payload(
      int colorCode,
      int i3LightBarrier,
      int i6LightBarrier,
      int i7LightBarrier,
      int i8LightBarrier) {
    return """
        {
          "timestamp": "2026-03-26T10:00:00Z",
          "i1_light_barrier": 0,
          "i2_color_sensor": %d,
          "i3_light_barrier": %d,
          "i6_light_barrier": %d,
          "i7_light_barrier": %d,
          "i8_light_barrier": %d,
          "current_task": "sorting",
          "current_task_duration": 1.2
        }
        """.formatted(colorCode, i3LightBarrier, i6LightBarrier, i7LightBarrier, i8LightBarrier);
  }
}
