package org.unisg.ftengrave.vacuumgripperintegrationservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.unisg.ftengrave.vacuumgripperintegrationservice.dto.VacuumGripperCommandDto;

class VacuumGripperServiceTest {

  @Test
  void handleRoutesTransportCommandToHttpAdapter() {
    RecordingVacuumGripperHttpService vacuumGripperHttpService = new RecordingVacuumGripperHttpService();
    VacuumGripperService vacuumGripperService = new VacuumGripperService(vacuumGripperHttpService);

    vacuumGripperService.handle(
        new VacuumGripperCommandDto(VacuumGripperService.MOVE_ITEM_FROM_INPUT_TO_ENGRAVER_COMMAND));

    assertEquals(1, vacuumGripperHttpService.moveCalls);
  }

  @Test
  void handleIgnoresUnknownCommandType() {
    RecordingVacuumGripperHttpService vacuumGripperHttpService = new RecordingVacuumGripperHttpService();
    VacuumGripperService vacuumGripperService = new VacuumGripperService(vacuumGripperHttpService);

    vacuumGripperService.handle(new VacuumGripperCommandDto("unknown-command"));

    assertEquals(0, vacuumGripperHttpService.moveCalls);
  }

  @Test
  void commandDtoAcceptsLegacyEventTypeFieldWhenDeserializing() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();

    VacuumGripperCommandDto vacuumGripperCommandDto =
        objectMapper.readValue(
            """
            {"eventType":"move-item-from-input-to-engraver-command"}
            """,
            VacuumGripperCommandDto.class);

    assertEquals(
        VacuumGripperService.MOVE_ITEM_FROM_INPUT_TO_ENGRAVER_COMMAND,
        vacuumGripperCommandDto.getCommandType());
  }

  private static final class RecordingVacuumGripperHttpService extends VacuumGripperHttpService {

    private int moveCalls;

    private RecordingVacuumGripperHttpService() {
      super(
          new RestTemplate(),
          "http",
          "localhost",
          8081,
          "/vgr/pick_up_and_transport",
          "vgr_1",
          "delivery_pick_up_station",
          "oven");
    }

    @Override
    public void moveItemFromInputToEngraver() {
      moveCalls++;
    }
  }
}
