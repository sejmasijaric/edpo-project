package org.unisg.ftengrave.kafkainspectorservice.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(OutputCaptureExtension.class)
class TopicEventConsumerTest {

  private final TopicEventConsumer consumer = new TopicEventConsumer();
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Test
  void logsReceivedEvents(CapturedOutput output) throws Exception {
    consumer.receive(objectMapper.readTree("{\"event\":\"received\"}"), "item-1", "test-topic");

    assertThat(output).contains("Received Kafka event on topic 'test-topic' with key 'item-1'");
    assertThat(output).contains("\"event\":\"received\"");
  }
}
