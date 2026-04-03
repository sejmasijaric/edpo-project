package org.unisg.ftengrave.qcservice.adapter.in.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.adapter.in.kafka.dto.PerformQcCommandDto;
import org.unisg.ftengrave.qcservice.port.in.StartQcUseCase;

@Component
@RequiredArgsConstructor
public class PerformQcCommandConsumer {

    private final StartQcUseCase startQcUseCase;

    @KafkaListener(
            topics = "${kafka.topic.stage-orchestration}",
            containerFactory = "performQcCommandKafkaListenerContainerFactory")
    public void consume(PerformQcCommandDto command) {
        startQcUseCase.startQc(command.itemIdentifier(), command.targetColor());
    }
}
