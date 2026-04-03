package org.unisg.ftengrave.orderorchestrator.adapter.in.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.orderorchestrator.adapter.in.kafka.dto.QcOutcomeEventDto;
import org.unisg.ftengrave.orderorchestrator.port.in.HandleQcOutcomeEventUseCase;

@Component
@RequiredArgsConstructor
public class QcOutcomeEventConsumer {

    private final HandleQcOutcomeEventUseCase handleQcOutcomeEventUseCase;

    @KafkaListener(
            topics = "${kafka.topic.machine-orchestration}",
            containerFactory = "qcOutcomeKafkaListenerContainerFactory")
    public void consume(QcOutcomeEventDto event) {
        handleQcOutcomeEventUseCase.handle(event.itemIdentifier(), event.outcomeType());
    }
}
