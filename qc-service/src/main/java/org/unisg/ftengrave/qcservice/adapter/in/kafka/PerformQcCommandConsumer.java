package org.unisg.ftengrave.qcservice.adapter.in.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.adapter.in.kafka.dto.PerformQcCommandDto;
import org.unisg.ftengrave.qcservice.port.in.StartQcUseCase;

@Component
@RequiredArgsConstructor
public class PerformQcCommandConsumer {

    private static final String RUN_ITEM_QC_COMMAND = "run-item-qc-command";

    private final StartQcUseCase startQcUseCase;

    @KafkaListener(
            topics = "${kafka.topic.stage-orchestration}",
            containerFactory = "performQcCommandKafkaListenerContainerFactory")
    public void consume(PerformQcCommandDto command) {
        if (command == null || !RUN_ITEM_QC_COMMAND.equals(command.commandType())) {
            return;
        }
        startQcUseCase.startQc(command.itemIdentifier(), command.targetColor());
    }
}
