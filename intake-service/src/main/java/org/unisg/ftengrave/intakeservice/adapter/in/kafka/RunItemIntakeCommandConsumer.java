package org.unisg.ftengrave.intakeservice.adapter.in.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.intakeservice.adapter.in.kafka.dto.RunItemIntakeCommandDto;
import org.unisg.ftengrave.intakeservice.port.in.StartIntakeUseCase;

@Component
@RequiredArgsConstructor
public class RunItemIntakeCommandConsumer {

    private static final String RUN_ITEM_INTAKE_COMMAND = "run-item-intake-command";

    private final StartIntakeUseCase startIntakeUseCase;

    @KafkaListener(
            topics = "${kafka.topic.stage-orchestration}",
            containerFactory = "intakeCommandKafkaListenerContainerFactory")
    public void consume(RunItemIntakeCommandDto command) {
        if (command == null || !RUN_ITEM_INTAKE_COMMAND.equals(command.getCommandType())) {
            return;
        }
        startIntakeUseCase.startIntake(command.getItemIdentifier(), command.getTargetColor());
    }
}
