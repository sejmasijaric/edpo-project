package org.unisg.ftengrave.qcservice.adapter.out.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaOperations;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.dto.QcOutcomeEventDto;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class QcRejectionOutcomePublisherAdapterTest {

    @Test
    void publishSendsRejectionOutcomeToMachineOrchestrationTopic() {
        @SuppressWarnings("unchecked")
        KafkaOperations<String, QcOutcomeEventDto> kafkaOperations = mock(KafkaOperations.class);
        QcRejectionOutcomePublisherAdapter adapter =
                new QcRejectionOutcomePublisherAdapter(kafkaOperations, "machine-orchestration");

        adapter.publish("item-42");

        verify(kafkaOperations).send(
                "machine-orchestration",
                "item-42",
                new QcOutcomeEventDto("item-42", QcOutcomeTypeNames.REJECTION));
    }
}
