package org.unisg.ftengrave.kafkainspectorservice.web;

import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.unisg.ftengrave.kafkainspectorservice.publisher.PublishSorterCommandUseCase;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PublishSorterCommandControllerTest {

  @Test
  void publishesNamedCommand() throws Exception {
    RecordingPublisher publisher = new RecordingPublisher();
    MockMvc mockMvc =
        MockMvcBuilders.standaloneSetup(new PublishSorterCommandController(publisher)).build();

    mockMvc.perform(post("/api/kafka/commands/sort-to-shipping"))
        .andExpect(status().isAccepted());

    assertThat(publisher.commandName).isEqualTo("sort-to-shipping");
  }

  private static final class RecordingPublisher implements PublishSorterCommandUseCase {

    private String commandName;

    @Override
    public void publish(String commandName) {
      this.commandName = commandName;
    }
  }
}
