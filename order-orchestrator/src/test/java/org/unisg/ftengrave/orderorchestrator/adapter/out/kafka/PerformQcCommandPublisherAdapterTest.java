package org.unisg.ftengrave.orderorchestrator.adapter.out.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaOperations;
import org.unisg.ftengrave.orderorchestrator.adapter.out.kafka.dto.PerformQcCommandDto;
import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class PerformQcCommandPublisherAdapterTest {

    @Test
    void publishSendsPerformQcCommandToStageOrchestrationTopic() {
        @SuppressWarnings("unchecked")
        KafkaOperations<String, PerformQcCommandDto> kafkaOperations = mock(KafkaOperations.class);
        PerformQcCommandPublisherAdapter adapter =
                new PerformQcCommandPublisherAdapter(kafkaOperations, "stage-orchestration");

        adapter.publish("item-42", ItemColor.RED);

        verify(kafkaOperations).send(
                "stage-orchestration",
                "item-42",
                new PerformQcCommandDto("run-item-qc-command", "item-42", ItemColor.RED));
    }
}
