package org.unisg.ftengrave.orderorchestrator.adapter.in.kafka;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.unisg.ftengrave.orderorchestrator.adapter.in.kafka.dto.ServiceOutcomeEventDto;
import org.unisg.ftengrave.orderorchestrator.port.in.HandleServiceOutcomeEventUseCase;

import static org.mockito.Mockito.verify;

class ServiceOutcomeEventConsumerTest {

    @Test
    void consumeDelegatesOutcomeEvent() {
        HandleServiceOutcomeEventUseCase useCase = Mockito.mock(HandleServiceOutcomeEventUseCase.class);
        ServiceOutcomeEventConsumer consumer = new ServiceOutcomeEventConsumer(useCase);

        consumer.consume(new ServiceOutcomeEventDto("item-42", "manufacturing-completed"));

        verify(useCase).handle("item-42", "manufacturing-completed");
    }
}
