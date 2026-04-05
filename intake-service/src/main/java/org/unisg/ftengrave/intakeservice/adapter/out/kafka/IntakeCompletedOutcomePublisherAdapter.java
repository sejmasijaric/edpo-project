package org.unisg.ftengrave.intakeservice.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.intakeservice.adapter.out.kafka.dto.IntakeOutcomeEventDto;
import org.unisg.ftengrave.intakeservice.port.out.PublishIntakeCompletedOutcomePort;
import org.unisg.ftengrave.sharedkafka.publisher.TransactionAwareKafkaPublisher;

@Component
public class IntakeCompletedOutcomePublisherAdapter
        extends TransactionAwareKafkaPublisher<String, IntakeOutcomeEventDto>
        implements PublishIntakeCompletedOutcomePort {

    private final String machineOrchestrationTopic;

    public IntakeCompletedOutcomePublisherAdapter(
            KafkaOperations<String, IntakeOutcomeEventDto> kafkaOperations,
            @Value("${kafka.topic.machine-orchestration}") String machineOrchestrationTopic) {
        super(kafkaOperations);
        this.machineOrchestrationTopic = machineOrchestrationTopic;
    }

    @Override
    public void publish(String itemIdentifier) {
        publishAfterCommitOrNow(() -> send(
                machineOrchestrationTopic,
                itemIdentifier,
                new IntakeOutcomeEventDto(itemIdentifier, IntakeOutcomeTypeNames.COMPLETED)));
    }
}
