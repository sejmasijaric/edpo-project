package org.unisg.ftengrave.manufacturingservice.adapter.out.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaOperations;
import org.unisg.ftengrave.manufacturingservice.adapter.out.kafka.dto.ManufacturingOutcomeEventDto;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class ManufacturingOutcomePublisherAdaptersTest {

    @Test
    void completedPublisherSendsCompletedOutcomeToMachineOrchestrationTopic() {
        @SuppressWarnings("unchecked")
        KafkaOperations<String, ManufacturingOutcomeEventDto> kafkaOperations = mock(KafkaOperations.class);
        ManufacturingCompletedOutcomePublisherAdapter adapter =
                new ManufacturingCompletedOutcomePublisherAdapter(kafkaOperations, "machine-orchestration");

        adapter.publish("item-42");

        verify(kafkaOperations).send(
                "machine-orchestration",
                "item-42",
                new ManufacturingOutcomeEventDto("item-42", "manufacturing-completed"));
    }

    @Test
    void failedPublisherSendsFailedOutcomeToMachineOrchestrationTopic() {
        @SuppressWarnings("unchecked")
        KafkaOperations<String, ManufacturingOutcomeEventDto> kafkaOperations = mock(KafkaOperations.class);
        ManufacturingFailedOutcomePublisherAdapter adapter =
                new ManufacturingFailedOutcomePublisherAdapter(kafkaOperations, "machine-orchestration");

        adapter.publish("item-42");

        verify(kafkaOperations).send(
                "machine-orchestration",
                "item-42",
                new ManufacturingOutcomeEventDto("item-42", "manufacturing-failed"));
    }
}
