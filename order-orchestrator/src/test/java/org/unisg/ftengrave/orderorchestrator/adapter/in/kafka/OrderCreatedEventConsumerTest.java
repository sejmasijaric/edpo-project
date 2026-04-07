package org.unisg.ftengrave.orderorchestrator.adapter.in.kafka;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.unisg.ftengrave.orderorchestrator.adapter.in.kafka.dto.OrderCreatedEventDto;
import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;
import org.unisg.ftengrave.orderorchestrator.port.in.StartOrderOrchestrationUseCase;

class OrderCreatedEventConsumerTest {

    @Test
    void consumeDelegatesOrderCreatedEvent() {
        StartOrderOrchestrationUseCase useCase = Mockito.mock(StartOrderOrchestrationUseCase.class);
        OrderCreatedEventConsumer consumer = new OrderCreatedEventConsumer(useCase);

        consumer.consume(new OrderCreatedEventDto("item-42", "red"));

        verify(useCase).startOrderOrchestration("item-42", ItemColor.RED);
    }
}
