package org.unisg.ftengrave.manufacturingservice.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.manufacturingservice.adapter.out.kafka.dto.ManufacturingOutcomeEventDto;
import org.unisg.ftengrave.manufacturingservice.port.out.PublishManufacturingFailedOutcomePort;
import org.unisg.ftengrave.sharedkafka.publisher.TransactionAwareKafkaPublisher;

@Component
public class ManufacturingFailedOutcomePublisherAdapter
        extends TransactionAwareKafkaPublisher<String, ManufacturingOutcomeEventDto>
        implements PublishManufacturingFailedOutcomePort {

    private final String machineOrchestrationTopic;

    public ManufacturingFailedOutcomePublisherAdapter(
            KafkaOperations<String, ManufacturingOutcomeEventDto> kafkaOperations,
            @Value("${kafka.topic.machine-orchestration}") String machineOrchestrationTopic) {
        super(kafkaOperations);
        this.machineOrchestrationTopic = machineOrchestrationTopic;
    }

    @Override
    public void publish(String itemIdentifier) {
        publishAfterCommitOrNow(() -> send(
                machineOrchestrationTopic,
                itemIdentifier,
                new ManufacturingOutcomeEventDto(itemIdentifier, ManufacturingOutcomeTypeNames.FAILED)));
    }
}
