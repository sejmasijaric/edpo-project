package org.unisg.ftengrave.workstationtransportintegrationservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.unisg.ftengrave.workstationtransportintegrationservice.dto.WorkstationTransportCommandDto;

class WorkstationTransportServiceTest {

  @Test
  void handleRoutesMoveCommandToHttpAdapter() {
    RecordingWorkstationTransportHttpService workstationTransportHttpService =
        new RecordingWorkstationTransportHttpService();
    WorkstationTransportService workstationTransportService =
        new WorkstationTransportService(workstationTransportHttpService);

    workstationTransportService.handle(
        new WorkstationTransportCommandDto(
            WorkstationTransportService.MOVE_ITEM_FROM_ENGRAVER_TO_POLISHING_MACHINE_COMMAND));

    assertEquals(1, workstationTransportHttpService.moveCalls);
  }

  @Test
  void handleIgnoresUnknownCommandType() {
    RecordingWorkstationTransportHttpService workstationTransportHttpService =
        new RecordingWorkstationTransportHttpService();
    WorkstationTransportService workstationTransportService =
        new WorkstationTransportService(workstationTransportHttpService);

    workstationTransportService.handle(new WorkstationTransportCommandDto("unknown-command"));

    assertEquals(0, workstationTransportHttpService.moveCalls);
  }

  @Test
  void commandDtoAcceptsLegacyEventTypeFieldWhenDeserializing() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();

    WorkstationTransportCommandDto workstationTransportCommandDto =
        objectMapper.readValue(
            """
            {"eventType":"move-item-from-engraver-to-polishing-machine-command"}
            """,
            WorkstationTransportCommandDto.class);

    assertEquals(
        WorkstationTransportService.MOVE_ITEM_FROM_ENGRAVER_TO_POLISHING_MACHINE_COMMAND,
        workstationTransportCommandDto.getCommandType());
  }

  private static final class RecordingWorkstationTransportHttpService
      extends WorkstationTransportHttpService {

    private int moveCalls;

    private RecordingWorkstationTransportHttpService() {
      super(
          new RestTemplate(),
          "http",
          "localhost",
          8081,
          "/wt/pick_up_and_transport",
          "wt_1",
          "oven",
          "milling_machine");
    }

    @Override
    public void moveItemFromEngraverToPolishingMachine() {
      moveCalls++;
    }
  }
}
