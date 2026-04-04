package org.unisg.ftengrave.intakeservice.adapter.in.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.intakeservice.adapter.in.kafka.dto.VacuumGripperEventDto;
import org.unisg.ftengrave.intakeservice.port.in.HandleItemArrivedAtIntakeEventUseCase;
import org.unisg.ftengrave.intakeservice.port.in.HandleItemLeftIntakeEventUseCase;
import org.unisg.ftengrave.intakeservice.port.in.VacuumGripperEvent;

@Component
@RequiredArgsConstructor
public class VacuumGripperEventConsumer {

    private final HandleItemArrivedAtIntakeEventUseCase handleItemArrivedAtIntakeEventUseCase;
    private final HandleItemLeftIntakeEventUseCase handleItemLeftIntakeEventUseCase;

    @KafkaListener(topics = "${kafka.topic.vacuum-gripper}")
    public void consume(VacuumGripperEventDto event) {
        VacuumGripperEvent vacuumGripperEvent = new VacuumGripperEvent(event == null ? null : event.getEventType());
        handleItemArrivedAtIntakeEventUseCase.handle(vacuumGripperEvent);
        handleItemLeftIntakeEventUseCase.handle(vacuumGripperEvent);
    }
}
