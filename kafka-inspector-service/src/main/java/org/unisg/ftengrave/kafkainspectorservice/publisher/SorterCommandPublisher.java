package org.unisg.ftengrave.kafkainspectorservice.publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.kafkainspectorservice.dto.SortingMachineCommandDto;
import org.unisg.ftengrave.sharedkafka.publisher.TransactionAwareKafkaPublisher;

@Component
public class SorterCommandPublisher extends TransactionAwareKafkaPublisher<String, SortingMachineCommandDto>
    implements PublishSorterCommandUseCase {

  private static final Logger LOGGER = LoggerFactory.getLogger(SorterCommandPublisher.class);

  private final String publishTopic;

  public SorterCommandPublisher(
      KafkaOperations<String, SortingMachineCommandDto> kafkaOperations,
      @Value("${kafka.topic.command-publish}") String publishTopic) {
    super(kafkaOperations);
    this.publishTopic = publishTopic;
  }

  @Override
  public void publish(String commandName) {
    SortingMachineCommandDto payload = new SortingMachineCommandDto(commandName);
    publishAfterCommitOrNow(() -> send(publishTopic, payload));
    LOGGER.info(
        "Published sorter command '{}' to topic '{}'",
        commandName,
        publishTopic);
  }
}
