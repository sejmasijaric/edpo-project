package org.unisg.ftengrave.orderorchestrator.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.unisg.ftengrave.orderorchestrator.adapter.out.kafka.dto.PerformQcCommandDto;
import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;
import org.unisg.ftengrave.orderorchestrator.port.out.SendPerformQcCommandPort;

@Component
public class PerformQcCommandPublisherAdapter implements SendPerformQcCommandPort {

    private final KafkaOperations<String, PerformQcCommandDto> kafkaOperations;
    private final String stageOrchestrationTopic;

    public PerformQcCommandPublisherAdapter(
            KafkaOperations<String, PerformQcCommandDto> kafkaOperations,
            @Value("${kafka.topic.stage-orchestration}") String stageOrchestrationTopic) {
        this.kafkaOperations = kafkaOperations;
        this.stageOrchestrationTopic = stageOrchestrationTopic;
    }

    @Override
    public void publish(String itemIdentifier, ItemColor targetColor) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(itemIdentifier, targetColor);
                }
            });
            return;
        }

        send(itemIdentifier, targetColor);
    }

    private void send(String itemIdentifier, ItemColor targetColor) {
        kafkaOperations.send(
                stageOrchestrationTopic,
                itemIdentifier,
                new PerformQcCommandDto(itemIdentifier, targetColor));
    }
}
