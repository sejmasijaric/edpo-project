package org.unisg.ftengrave.kafkainspectorservice.publisher;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaOperations;
import org.unisg.ftengrave.kafkainspectorservice.dto.SortingMachineCommandDto;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class SorterCommandPublisherTest {

  @Test
  void publishesConfiguredSorterCommand() {
    @SuppressWarnings("unchecked")
    KafkaOperations<String, SortingMachineCommandDto> kafkaOperations = mock(KafkaOperations.class);

    SorterCommandPublisher publisher =
        new SorterCommandPublisher(kafkaOperations, "sorting-machine");

    publisher.publish("request-sort-to-shipping");

    verify(kafkaOperations)
        .send(
            org.mockito.ArgumentMatchers.eq("sorting-machine"),
            argThat(payload -> "request-sort-to-shipping".equals(payload.getCommandType())));
  }
}
