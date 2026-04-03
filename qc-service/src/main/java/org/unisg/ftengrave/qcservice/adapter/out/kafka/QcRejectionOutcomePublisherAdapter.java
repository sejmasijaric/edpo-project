package org.unisg.ftengrave.qcservice.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.dto.QcOutcomeEventDto;
import org.unisg.ftengrave.qcservice.port.out.PublishQcRejectionOutcomePort;
import org.unisg.ftengrave.sharedkafka.publisher.TransactionAwareKafkaPublisher;

@Component
public class QcRejectionOutcomePublisherAdapter extends TransactionAwareKafkaPublisher<String, QcOutcomeEventDto>
        implements PublishQcRejectionOutcomePort {
    private final String machineOrchestrationTopic;

    public QcRejectionOutcomePublisherAdapter(
            KafkaOperations<String, QcOutcomeEventDto> kafkaOperations,
            @Value("${kafka.topic.machine-orchestration}") String machineOrchestrationTopic) {
        super(kafkaOperations);
        this.machineOrchestrationTopic = machineOrchestrationTopic;
    }

    @Override
    public void publish(String itemIdentifier) {
        publishAfterCommitOrNow(() -> send(itemIdentifier));
    }

    private void send(String itemIdentifier) {
        send(
                machineOrchestrationTopic,
                itemIdentifier,
                new QcOutcomeEventDto(itemIdentifier, QcOutcomeTypeNames.REJECTION));
    }
}
