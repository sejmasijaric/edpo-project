package org.unisg.ftengrave.orderorchestrator.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.orderorchestrator.adapter.out.kafka.dto.RunProductionCommandDto;
import org.unisg.ftengrave.orderorchestrator.port.out.SendRunProductionCommandPort;
import org.unisg.ftengrave.sharedkafka.publisher.TransactionAwareKafkaPublisher;

@Component
public class RunProductionCommandPublisherAdapter
        extends TransactionAwareKafkaPublisher<String, RunProductionCommandDto>
        implements SendRunProductionCommandPort {

    private static final String RUN_PRODUCTION_COMMAND = "run-production-command";

    private final String stageOrchestrationTopic;

    public RunProductionCommandPublisherAdapter(
            KafkaOperations<String, RunProductionCommandDto> kafkaOperations,
            @Value("${kafka.topic.stage-orchestration}") String stageOrchestrationTopic) {
        super(kafkaOperations);
        this.stageOrchestrationTopic = stageOrchestrationTopic;
    }

    @Override
    public void publish(String itemIdentifier) {
        publishAfterCommitOrNow(() -> send(
                stageOrchestrationTopic,
                itemIdentifier,
                new RunProductionCommandDto(RUN_PRODUCTION_COMMAND, itemIdentifier)));
    }
}
