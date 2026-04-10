package org.unisg.ftengrave.manufacturingservice.adapter.in.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.manufacturingservice.adapter.in.kafka.dto.EngraverEventDto;
import org.unisg.ftengrave.manufacturingservice.port.in.EngraverEvent;
import org.unisg.ftengrave.manufacturingservice.port.in.HandleItemArrivedAtEngraverSinkEventUseCase;
import org.unisg.ftengrave.manufacturingservice.port.in.HandleItemLeftEngraverSinkEventUseCase;

@Component
@RequiredArgsConstructor
public class EngraverEventConsumer {

    private final HandleItemLeftEngraverSinkEventUseCase handleItemLeftEngraverSinkEventUseCase;
    private final HandleItemArrivedAtEngraverSinkEventUseCase handleItemArrivedAtEngraverSinkEventUseCase;

    @KafkaListener(
            topics = "${kafka.topic.engraver-event}",
            containerFactory = "engraverEventKafkaListenerContainerFactory")
    public void consume(EngraverEventDto event) {
        EngraverEvent engraverEvent = new EngraverEvent(event == null ? null : event.getEventType());
        handleItemLeftEngraverSinkEventUseCase.handle(engraverEvent);
        handleItemArrivedAtEngraverSinkEventUseCase.handle(engraverEvent);
    }
}
