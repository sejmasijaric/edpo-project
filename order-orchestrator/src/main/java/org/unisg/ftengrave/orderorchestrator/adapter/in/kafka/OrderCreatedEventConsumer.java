package org.unisg.ftengrave.orderorchestrator.adapter.in.kafka;

import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.orderorchestrator.DuplicateBusinessKeyException;
import org.unisg.ftengrave.orderorchestrator.adapter.in.kafka.dto.OrderCreatedEventDto;
import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;
import org.unisg.ftengrave.orderorchestrator.port.in.StartOrderOrchestrationUseCase;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderCreatedEventConsumer {

    private final StartOrderOrchestrationUseCase startOrderOrchestrationUseCase;

    @KafkaListener(
            topics = "${kafka.topic.order-created}",
            containerFactory = "orderCreatedKafkaListenerContainerFactory")
    public void consume(OrderCreatedEventDto event) {
        try {
            startOrderOrchestrationUseCase.startOrderOrchestration(
                    event.itemIdentifier(),
                    ItemColor.valueOf(event.targetColor().toUpperCase(Locale.ROOT)));
        } catch (DuplicateBusinessKeyException exception) {
            log.info("Ignoring duplicate order-created event for itemIdentifier={}", event.itemIdentifier());
        }
    }
}
