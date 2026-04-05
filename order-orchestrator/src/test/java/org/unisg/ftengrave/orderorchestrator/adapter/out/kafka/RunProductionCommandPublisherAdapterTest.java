package org.unisg.ftengrave.orderorchestrator.adapter.out.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaOperations;
import org.unisg.ftengrave.orderorchestrator.adapter.out.kafka.dto.RunProductionCommandDto;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class RunProductionCommandPublisherAdapterTest {

    @Test
    void publishSendsRunProductionCommandToStageOrchestrationTopic() {
        @SuppressWarnings("unchecked")
        KafkaOperations<String, RunProductionCommandDto> kafkaOperations = mock(KafkaOperations.class);
        RunProductionCommandPublisherAdapter adapter =
                new RunProductionCommandPublisherAdapter(kafkaOperations, "stage-orchestration");

        adapter.publish("item-42");

        verify(kafkaOperations).send(
                "stage-orchestration",
                "item-42",
                new RunProductionCommandDto("run-production-command", "item-42"));
    }
}
