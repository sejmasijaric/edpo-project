package org.unisg.ftengrave.intakeservice.adapter.out.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaOperations;
import org.unisg.ftengrave.intakeservice.adapter.out.kafka.dto.IntakeOutcomeEventDto;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class IntakeCompletedOutcomePublisherAdapterTest {

    @Test
    void publishSendsIntakeCompletedOutcomeToMachineOrchestrationTopic() {
        @SuppressWarnings("unchecked")
        KafkaOperations<String, IntakeOutcomeEventDto> kafkaOperations = mock(KafkaOperations.class);
        IntakeCompletedOutcomePublisherAdapter adapter =
                new IntakeCompletedOutcomePublisherAdapter(kafkaOperations, "machine-orchestration");

        adapter.publish("item-42");

        verify(kafkaOperations).send(
                "machine-orchestration",
                "item-42",
                new IntakeOutcomeEventDto("item-42", "intake-completed"));
    }
}
