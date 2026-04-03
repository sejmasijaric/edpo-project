package org.unisg.ftengrave.qcservice.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.dto.QcOutcomeEventDto;
import org.unisg.ftengrave.qcservice.port.out.PublishQcShippingOutcomePort;

@Component
public class QcShippingOutcomePublisherAdapter implements PublishQcShippingOutcomePort {

    private final KafkaOperations<String, QcOutcomeEventDto> kafkaOperations;
    private final String machineOrchestrationTopic;

    public QcShippingOutcomePublisherAdapter(
            KafkaOperations<String, QcOutcomeEventDto> kafkaOperations,
            @Value("${kafka.topic.machine-orchestration}") String machineOrchestrationTopic) {
        this.kafkaOperations = kafkaOperations;
        this.machineOrchestrationTopic = machineOrchestrationTopic;
    }

    @Override
    public void publish(String itemIdentifier) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    send(itemIdentifier);
                }
            });
            return;
        }

        send(itemIdentifier);
    }

    private void send(String itemIdentifier) {
        kafkaOperations.send(
                machineOrchestrationTopic,
                itemIdentifier,
                new QcOutcomeEventDto(itemIdentifier, QcOutcomeTypeNames.SHIPPING));
    }
}
