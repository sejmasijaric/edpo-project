package org.unisg.ftengrave.orderorchestrator.adapter.in.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.orderorchestrator.adapter.in.kafka.dto.ServiceOutcomeEventDto;
import org.unisg.ftengrave.orderorchestrator.port.in.HandleServiceOutcomeEventUseCase;

@Component
@RequiredArgsConstructor
public class ServiceOutcomeEventConsumer {

    private final HandleServiceOutcomeEventUseCase handleServiceOutcomeEventUseCase;

    @KafkaListener(
            topics = "${kafka.topic.machine-orchestration}",
            containerFactory = "serviceOutcomeKafkaListenerContainerFactory")
    public void consume(ServiceOutcomeEventDto event) {
        handleServiceOutcomeEventUseCase.handle(event.itemIdentifier(), event.outcomeType());
    }
}
