package org.unisg.ftengrave.orderorchestrator.adapter.out.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaOperations;
import org.unisg.ftengrave.orderorchestrator.adapter.out.kafka.dto.RunItemIntakeCommandDto;
import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RunItemIntakeCommandPublisherAdapterTest {

    @Test
    void publishSendsRunItemIntakeCommandToStageOrchestrationTopic() {
        @SuppressWarnings("unchecked")
        KafkaOperations<String, RunItemIntakeCommandDto> kafkaOperations = mock(KafkaOperations.class);
        RunItemIntakeCommandPublisherAdapter adapter =
                new RunItemIntakeCommandPublisherAdapter(kafkaOperations, "stage-orchestration");

        adapter.publish("item-42", ItemColor.RED);

        verify(kafkaOperations).send(
                "stage-orchestration",
                "item-42",
                new RunItemIntakeCommandDto("run-item-intake-command", "item-42", ItemColor.RED));
    }
}
