package org.unisg.ftengrave.sorterintegrationservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.unisg.ftengrave.sorterintegrationservice.dto.SortingMachineEventDto;

class SortingMachineServiceTest {

  @Test
  void handleRoutesRejectEventToSinkOne() {
    RecordingSorterHttpService sorterHttpService = new RecordingSorterHttpService();
    SortingMachineService sortingMachineService = new SortingMachineService(sorterHttpService);

    sortingMachineService.handle(new SortingMachineEventDto("sort-to-reject"));

    assertEquals("sink_1", sorterHttpService.lastSinkIdentifier);
  }

  @Test
  void handleRoutesColorDetectionRequestToDetectColorCall() {
    RecordingSorterHttpService sorterHttpService = new RecordingSorterHttpService();
    SortingMachineService sortingMachineService = new SortingMachineService(sorterHttpService);

    sortingMachineService.handle(new SortingMachineEventDto("request-color-detection"));

    assertEquals(1, sorterHttpService.detectColorCalls);
    assertNull(sorterHttpService.lastSinkIdentifier);
  }

  @Test
  void handleRejectsUnknownEventType() {
    RecordingSorterHttpService sorterHttpService = new RecordingSorterHttpService();
    SortingMachineService sortingMachineService = new SortingMachineService(sorterHttpService);

    assertThrows(
        IllegalArgumentException.class,
        () -> sortingMachineService.handle(new SortingMachineEventDto("unknown-event")));
    assertNull(sorterHttpService.lastSinkIdentifier);
  }

  private static final class RecordingSorterHttpService extends SorterHttpService {

    private String lastSinkIdentifier;
    private int detectColorCalls;

    private RecordingSorterHttpService() {
      super(new RestTemplate(), "http", "localhost", 8081, "/sm/detect_color", "/sm/sort", "sm_1", "initial");
    }

    @Override
    public void detectColor() {
      detectColorCalls++;
    }

    @Override
    public void sortToSink(String sinkIdentifier) {
      this.lastSinkIdentifier = sinkIdentifier;
    }
  }
}
