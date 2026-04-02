package org.unisg.ftengrave.sorterintegrationservice.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.sorterintegrationservice.dto.SortingMachineEventDto;

class SortingMachineEventFilterTest {

  private final SortingMachineEventFilter filter = new SortingMachineEventFilter(new ObjectMapper());

  @Test
  void emitsColorDetectedEventWhenColorSensorDropsBelowDetectionThreshold() {
    Optional<SortingMachineEventDto> event =
        filter.filter("FTFactory/SM_1", payload(1, 1300, 1, 1, 1, 1));

    assertTrue(event.isPresent());
    assertEquals("color-detected", event.get().getEventType());
    assertEquals("red", event.get().getColor());
  }

  @Test
  void emitsColorDetectedEventWhenThresholdCrossingOccursWithoutLightBarrierChange() {
    filter.filter("FTFactory/SM_1", payload(1, 1800, 1, 1, 1, 1));

    Optional<SortingMachineEventDto> event =
        filter.filter("FTFactory/SM_1", payload(1, 0, 1, 1, 1, 1));

    assertTrue(event.isPresent());
    assertEquals("color-detected", event.get().getEventType());
    assertEquals("white", event.get().getColor());
  }

  @Test
  void doesNotEmitAdditionalColorDetectedEventWhileSensorValueRemainsBelowThreshold() {
    filter.filter("FTFactory/SM_1", payload(1, 1800, 1, 1, 1, 1));
    filter.filter("FTFactory/SM_1", payload(1, 1400, 1, 1, 1, 1));

    Optional<SortingMachineEventDto> event =
        filter.filter("FTFactory/SM_1", payload(1, 1300, 1, 1, 1, 1));

    assertTrue(event.isEmpty());
  }

  @Test
  void emitsArrivalAndReleaseEventsForRejectionSinkBarrier() {
    Optional<SortingMachineEventDto> arrivalEvent =
        filter.filter("FTFactory/SM_1", payload(1, 1800, 1, 0, 1, 1));

    Optional<SortingMachineEventDto> releaseEvent =
        filter.filter("FTFactory/SM_1", payload(1, 1800, 1, 1, 1, 1));

    assertTrue(arrivalEvent.isPresent());
    assertEquals("item-arrived-at-rejection-sink", arrivalEvent.get().getEventType());
    assertTrue(releaseEvent.isPresent());
    assertEquals("item-left-rejection-sink", releaseEvent.get().getEventType());
  }

  @Test
  void emitsArrivalAndReleaseEventsForColorSensorBarrier() {
    Optional<SortingMachineEventDto> arrivalEvent =
        filter.filter("FTFactory/SM_1", payload(0, 1800, 1, 1, 1, 1));

    Optional<SortingMachineEventDto> releaseEvent =
        filter.filter("FTFactory/SM_1", payload(1, 1800, 1, 1, 1, 1));

    assertTrue(arrivalEvent.isPresent());
    assertEquals("item-arrived-at-color-sensor", arrivalEvent.get().getEventType());
    assertTrue(releaseEvent.isPresent());
    assertEquals("item-left-color-sensor", releaseEvent.get().getEventType());
  }

  @Test
  void emitsArrivalAndReleaseEventsForQcBarrier() {
    Optional<SortingMachineEventDto> arrivalEvent =
        filter.filter("FTFactory/SM_1", payload(1, 1800, 0, 1, 1, 1));

    Optional<SortingMachineEventDto> releaseEvent =
        filter.filter("FTFactory/SM_1", payload(1, 1800, 1, 1, 1, 1));

    assertTrue(arrivalEvent.isPresent());
    assertEquals("item-arrived-at-qc", arrivalEvent.get().getEventType());
    assertTrue(releaseEvent.isPresent());
    assertEquals("item-left-qc", releaseEvent.get().getEventType());
  }

  @Test
  void ignoresUnchangedSnapshots() {
    filter.filter("FTFactory/SM_1", payload(1, 1800, 1, 1, 1, 1));

    Optional<SortingMachineEventDto> event =
        filter.filter("FTFactory/SM_1", payload(1, 1800, 1, 1, 1, 1));

    assertTrue(event.isEmpty());
  }

  private String payload(
      int i1LightBarrier,
      int i2ColorSensor,
      int i3LightBarrier,
      int i6LightBarrier,
      int i7LightBarrier,
      int i8LightBarrier) {
    return """
        {
          "timestamp":"2026-04-02T10:15:30Z",
          "i1_light_barrier":%d,
          "i2_color_sensor":%d,
          "i3_light_barrier":%d,
          "i6_light_barrier":%d,
          "i7_light_barrier":%d,
          "i8_light_barrier":%d,
          "current_task":"idle",
          "current_task_duration":0.0
        }
        """.formatted(i1LightBarrier, i2ColorSensor, i3LightBarrier, i6LightBarrier, i7LightBarrier, i8LightBarrier);
  }
}
