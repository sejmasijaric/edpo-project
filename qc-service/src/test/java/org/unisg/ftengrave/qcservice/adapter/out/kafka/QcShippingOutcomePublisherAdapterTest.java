package org.unisg.ftengrave.qcservice.adapter.out.kafka;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaOperations;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.dto.QcOutcomeEventDto;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class QcShippingOutcomePublisherAdapterTest {

    @Test
    void publishSendsShippingOutcomeToMachineOrchestrationTopic() {
        @SuppressWarnings("unchecked")
        KafkaOperations<String, QcOutcomeEventDto> kafkaOperations = mock(KafkaOperations.class);
        QcShippingOutcomePublisherAdapter adapter =
                new QcShippingOutcomePublisherAdapter(kafkaOperations, "machine-orchestration");

        adapter.publish("item-42");

        verify(kafkaOperations).send(
                "machine-orchestration",
                "item-42",
                new QcOutcomeEventDto("item-42", QcOutcomeTypeNames.SHIPPING));
    }
}
