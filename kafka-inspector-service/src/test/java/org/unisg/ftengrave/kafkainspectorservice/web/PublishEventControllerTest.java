package org.unisg.ftengrave.kafkainspectorservice.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.unisg.ftengrave.kafkainspectorservice.publisher.PublishTopicEventUseCase;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import static org.assertj.core.api.Assertions.assertThat;

class PublishEventControllerTest {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void publishesIncomingPayload() throws Exception {
    RecordingPublisher publisher = new RecordingPublisher();
    MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PublishEventController(publisher)).build();

    String body = """
        {
          "key": "item-42",
          "payload": {
            "type": "custom-event",
            "status": "ok"
          }
        }
        """;

    mockMvc.perform(post("/api/kafka/messages")
            .contentType(MediaType.APPLICATION_JSON)
            .content(body))
        .andExpect(status().isAccepted());

    assertThat(publisher.key).isEqualTo("item-42");
    assertThat(publisher.payload).isEqualTo(objectMapper.readTree("{\"type\":\"custom-event\",\"status\":\"ok\"}"));
  }

  private static final class RecordingPublisher implements PublishTopicEventUseCase {

    private String key;
    private com.fasterxml.jackson.databind.JsonNode payload;

    @Override
    public void publish(String key, com.fasterxml.jackson.databind.JsonNode payload) {
      this.key = key;
      this.payload = payload;
    }
  }
}
