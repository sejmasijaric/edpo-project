package org.unisg.ftengrave.manufacturingservice.adapter.in.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.manufacturingservice.adapter.in.kafka.dto.RunProductionCommandDto;
import org.unisg.ftengrave.manufacturingservice.port.in.StartManufacturingUseCase;

@Component
@RequiredArgsConstructor
public class RunProductionCommandConsumer {

    private static final String RUN_PRODUCTION_COMMAND = "run-production-command";

    private final StartManufacturingUseCase startManufacturingUseCase;

    @KafkaListener(
            topics = "${kafka.topic.stage-orchestration}",
            containerFactory = "runProductionCommandKafkaListenerContainerFactory")
    public void consume(RunProductionCommandDto command) {
        if (command == null || !RUN_PRODUCTION_COMMAND.equals(command.getCommandType())) {
            return;
        }
        startManufacturingUseCase.startManufacturing(command.getItemIdentifier());
    }
}
