package org.unisg.ftengrave.engraverintegrationservice.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestTemplate;
import org.unisg.ftengrave.engraverintegrationservice.dto.EngraverCommandDto;

class EngraverServiceTest {

  @Test
  void handleRoutesRunCommandToHttpAdapter() {
    RecordingEngraverHttpService engraverHttpService = new RecordingEngraverHttpService();
    EngraverService engraverService = new EngraverService(engraverHttpService);

    engraverService.handle(new EngraverCommandDto(EngraverService.RUN_ENGRAVING_COMMAND));

    assertEquals(1, engraverHttpService.runCalls);
  }

  @Test
  void handleIgnoresUnknownCommandType() {
    RecordingEngraverHttpService engraverHttpService = new RecordingEngraverHttpService();
    EngraverService engraverService = new EngraverService(engraverHttpService);

    engraverService.handle(new EngraverCommandDto("unknown-command"));

    assertEquals(0, engraverHttpService.runCalls);
  }

  @Test
  void commandDtoAcceptsLegacyEventTypeFieldWhenDeserializing() throws Exception {
    ObjectMapper objectMapper = new ObjectMapper();

    EngraverCommandDto engraverCommandDto =
        objectMapper.readValue(
            """
            {"eventType":"run-engraving-command"}
            """,
            EngraverCommandDto.class);

    assertEquals(EngraverService.RUN_ENGRAVING_COMMAND, engraverCommandDto.getCommandType());
  }

  private static final class RecordingEngraverHttpService extends EngraverHttpService {

    private int runCalls;

    private RecordingEngraverHttpService() {
      super(new RestTemplate(), "http", "localhost", 8081, "/ov/burn", "ov_1");
    }

    @Override
    public void runEngraving() {
      runCalls++;
    }
  }
}
