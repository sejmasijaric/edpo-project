package org.unisg.ftengrave.polishingmachineintegrationservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.unisg.ftengrave.polishingmachineintegrationservice.dto.PolishingMachineCommandDto;

class PolishingMachineServiceTest {

  @Test
  void handleRoutesRunCommandToHttpAdapter() {
    RecordingPolishingMachineHttpService polishingMachineHttpService =
        new RecordingPolishingMachineHttpService();
    PolishingMachineService polishingMachineService =
        new PolishingMachineService(polishingMachineHttpService);

    polishingMachineService.handle(
        new PolishingMachineCommandDto(PolishingMachineService.RUN_POLISHING_COMMAND));

    assertEquals(1, polishingMachineHttpService.runCalls);
  }

  @Test
  void handleIgnoresUnknownCommandType() {
    RecordingPolishingMachineHttpService polishingMachineHttpService =
        new RecordingPolishingMachineHttpService();
    PolishingMachineService polishingMachineService =
        new PolishingMachineService(polishingMachineHttpService);

    polishingMachineService.handle(new PolishingMachineCommandDto("unknown-command"));

    assertEquals(0, polishingMachineHttpService.runCalls);
  }

  @Test
  void commandDtoAcceptsLegacyEventTypeFieldWhenDeserializing() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();

    PolishingMachineCommandDto polishingMachineCommandDto =
        objectMapper.readValue(
            """
            {"eventType":"run-polishing-command"}
            """,
            PolishingMachineCommandDto.class);

    assertEquals(
        PolishingMachineService.RUN_POLISHING_COMMAND,
        polishingMachineCommandDto.getCommandType());
  }

  private static final class RecordingPolishingMachineHttpService extends PolishingMachineHttpService {

    private int runCalls;

    private RecordingPolishingMachineHttpService() {
      super(
          new RestTemplate(),
          "http",
          "localhost",
          8081,
          "/mm/mill",
          "mm_1",
          10,
          "initial",
          "ejection");
    }

    @Override
    public void runPolishing() {
      runCalls++;
    }
  }
}
