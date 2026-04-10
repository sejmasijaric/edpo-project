package org.unisg.ftengrave.manufacturingservice.adapter.in.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.manufacturingservice.adapter.in.kafka.dto.PolishingMachineEventDto;
import org.unisg.ftengrave.manufacturingservice.port.in.HandleItemArrivedAtPolishingMachineOutputEventUseCase;
import org.unisg.ftengrave.manufacturingservice.port.in.PolishingMachineEvent;

@Component
@RequiredArgsConstructor
public class PolishingMachineEventConsumer {

    private final HandleItemArrivedAtPolishingMachineOutputEventUseCase handleItemArrivedAtPolishingMachineOutputEventUseCase;

    @KafkaListener(
            topics = "${kafka.topic.polishing-machine-event}",
            containerFactory = "polishingMachineEventKafkaListenerContainerFactory")
    public void consume(PolishingMachineEventDto event) {
        handleItemArrivedAtPolishingMachineOutputEventUseCase.handle(
                new PolishingMachineEvent(event == null ? null : event.getEventType()));
    }
}
