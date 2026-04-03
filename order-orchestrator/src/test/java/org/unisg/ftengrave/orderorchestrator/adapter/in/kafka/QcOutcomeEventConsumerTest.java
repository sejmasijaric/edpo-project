package org.unisg.ftengrave.orderorchestrator.adapter.in.kafka;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.unisg.ftengrave.orderorchestrator.adapter.in.kafka.dto.QcOutcomeEventDto;
import org.unisg.ftengrave.orderorchestrator.port.in.HandleQcOutcomeEventUseCase;

import static org.mockito.Mockito.verify;

class QcOutcomeEventConsumerTest {

    @Test
    void consumeDelegatesOutcomeEvent() {
        HandleQcOutcomeEventUseCase useCase = Mockito.mock(HandleQcOutcomeEventUseCase.class);
        QcOutcomeEventConsumer consumer = new QcOutcomeEventConsumer(useCase);

        consumer.consume(new QcOutcomeEventDto("item-42", "qc-shipping"));

        verify(useCase).handle("item-42", "qc-shipping");
    }
}
