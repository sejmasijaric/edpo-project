package org.unisg.ftengrave.workstationtransportintegrationservice.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.workstationtransportintegrationservice.dto.WorkstationTransportEventDto;

class WorkstationTransportEventFilterTest {

  private final WorkstationTransportEventFilter filter =
      new WorkstationTransportEventFilter(new ObjectMapper());

  @Test
  void emitsMoveCompletedEventOnRisingEdgeOfReadyAndIdleState() {
    filter.filter("FTFactory/WT_1", payload("busy", "pick_up_and_transport"));

    Optional<WorkstationTransportEventDto> event =
        filter.filter("FTFactory/WT_1", payload("ready", ""));

    assertTrue(event.isPresent());
    assertEquals("wt-move-completed", event.get().getEventType());
  }

  @Test
  void doesNotEmitRepeatedEventWhileStateRemainsReadyAndIdle() {
    filter.filter("FTFactory/WT_1", payload("busy", "pick_up_and_transport"));
    filter.filter("FTFactory/WT_1", payload("ready", ""));

    Optional<WorkstationTransportEventDto> event =
        filter.filter("FTFactory/WT_1", payload("ready", ""));

    assertTrue(event.isEmpty());
  }

  @Test
  void ignoresIncompletePayloads() {
    Optional<WorkstationTransportEventDto> event =
        filter.filter("FTFactory/WT_1", "{\"timestamp\":\"2026-04-03T10:15:30Z\"}");

    assertTrue(event.isEmpty());
  }

  private String payload(String currentState, String currentTask) {
    return """
        {
          "timestamp":"2026-04-03T10:15:30Z",
          "current_state":"%s",
          "current_task":"%s",
          "current_task_duration":0.0
        }
        """.formatted(currentState, currentTask);
  }
}
