package org.unisg.ftengrave.orderorchestrator.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.orderorchestrator.adapter.out.kafka.dto.PerformQcCommandDto;
import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;
import org.unisg.ftengrave.orderorchestrator.port.out.SendPerformQcCommandPort;
import org.unisg.ftengrave.sharedkafka.publisher.TransactionAwareKafkaPublisher;

@Component
public class PerformQcCommandPublisherAdapter extends TransactionAwareKafkaPublisher<String, PerformQcCommandDto>
        implements SendPerformQcCommandPort {
    private static final String RUN_ITEM_QC_COMMAND = "run-item-qc-command";

    private final String stageOrchestrationTopic;

    public PerformQcCommandPublisherAdapter(
            KafkaOperations<String, PerformQcCommandDto> kafkaOperations,
            @Value("${kafka.topic.stage-orchestration}") String stageOrchestrationTopic) {
        super(kafkaOperations);
        this.stageOrchestrationTopic = stageOrchestrationTopic;
    }

    @Override
    public void publish(String itemIdentifier, ItemColor targetColor) {
        publishAfterCommitOrNow(() -> send(itemIdentifier, targetColor));
    }

    private void send(String itemIdentifier, ItemColor targetColor) {
        send(
                stageOrchestrationTopic,
                itemIdentifier,
                new PerformQcCommandDto(RUN_ITEM_QC_COMMAND, itemIdentifier, targetColor));
    }
}
