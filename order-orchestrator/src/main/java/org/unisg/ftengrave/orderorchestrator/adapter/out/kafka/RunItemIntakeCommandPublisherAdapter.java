package org.unisg.ftengrave.orderorchestrator.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.orderorchestrator.adapter.out.kafka.dto.RunItemIntakeCommandDto;
import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;
import org.unisg.ftengrave.orderorchestrator.port.out.SendRunIntakeCommandPort;
import org.unisg.ftengrave.sharedkafka.publisher.TransactionAwareKafkaPublisher;

@Component
public class RunItemIntakeCommandPublisherAdapter
        extends TransactionAwareKafkaPublisher<String, RunItemIntakeCommandDto>
        implements SendRunIntakeCommandPort {

    private static final String RUN_ITEM_INTAKE_COMMAND = "run-item-intake-command";

    private final String stageOrchestrationTopic;

    public RunItemIntakeCommandPublisherAdapter(
            KafkaOperations<String, RunItemIntakeCommandDto> kafkaOperations,
            @Value("${kafka.topic.stage-orchestration}") String stageOrchestrationTopic) {
        super(kafkaOperations);
        this.stageOrchestrationTopic = stageOrchestrationTopic;
    }

    @Override
    public void publish(String itemIdentifier, ItemColor targetColor) {
        publishAfterCommitOrNow(() -> send(
                stageOrchestrationTopic,
                itemIdentifier,
                new RunItemIntakeCommandDto(RUN_ITEM_INTAKE_COMMAND, itemIdentifier, targetColor)));
    }
}
