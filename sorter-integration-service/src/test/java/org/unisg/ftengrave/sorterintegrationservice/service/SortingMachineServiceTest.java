package org.unisg.ftengrave.sorterintegrationservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.unisg.ftengrave.sorterintegrationservice.dto.SortingMachineCommandDto;

class SortingMachineServiceTest {

  @Test
  void handleRoutesRejectCommandToSinkOne() {
    RecordingSorterHttpService sorterHttpService = new RecordingSorterHttpService();
    SortingMachineService sortingMachineService = new SortingMachineService(sorterHttpService);

    sortingMachineService.handle(new SortingMachineCommandDto("request-sort-to-reject"));

    assertEquals("sink_1", sorterHttpService.lastSinkIdentifier);
  }

  @Test
  void handleIgnoresSorterEventsOnCommandChannel() {
    RecordingSorterHttpService sorterHttpService = new RecordingSorterHttpService();
    SortingMachineService sortingMachineService = new SortingMachineService(sorterHttpService);

    sortingMachineService.handle(new SortingMachineCommandDto("detected-color-red"));

    assertNull(sorterHttpService.lastSinkIdentifier);
  }

  @Test
  void handleRoutesColorDetectionRequestToDetectColorCall() {
    RecordingSorterHttpService sorterHttpService = new RecordingSorterHttpService();
    SortingMachineService sortingMachineService = new SortingMachineService(sorterHttpService);

    sortingMachineService.handle(new SortingMachineCommandDto("request-color-detection"));

    assertEquals(1, sorterHttpService.detectColorCalls);
    assertNull(sorterHttpService.lastSinkIdentifier);
  }

  @Test
  void commandDtoAcceptsLegacyEventTypeFieldWhenDeserializing() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();

    SortingMachineCommandDto sortingMachineCommandDto =
        objectMapper.readValue(
            """
            {"eventType":"request-sort-to-shipping"}
            """,
            SortingMachineCommandDto.class);

    assertEquals("request-sort-to-shipping", sortingMachineCommandDto.getCommandType());
  }

  @Test
  void handleIgnoresUnknownCommandType() {
    RecordingSorterHttpService sorterHttpService = new RecordingSorterHttpService();
    SortingMachineService sortingMachineService = new SortingMachineService(sorterHttpService);

    sortingMachineService.handle(new SortingMachineCommandDto("unknown-command"));

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
