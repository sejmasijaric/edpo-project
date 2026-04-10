package org.unisg.ftengrave.intakeservice.adapter.in.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.intakeservice.adapter.in.kafka.dto.EngraverEventDto;
import org.unisg.ftengrave.intakeservice.port.in.EngraverEvent;
import org.unisg.ftengrave.intakeservice.port.in.HandleItemArrivedAtEngraverEventUseCase;

@Component
@RequiredArgsConstructor
public class EngraverEventConsumer {

    private final HandleItemArrivedAtEngraverEventUseCase handleItemArrivedAtEngraverEventUseCase;

    @KafkaListener(
            topics = "${kafka.topic.engraver-event}",
            containerFactory = "engraverEventKafkaListenerContainerFactory")
    public void consume(EngraverEventDto event) {
        handleItemArrivedAtEngraverEventUseCase.handle(new EngraverEvent(event == null ? null : event.getEventType()));
    }
}
