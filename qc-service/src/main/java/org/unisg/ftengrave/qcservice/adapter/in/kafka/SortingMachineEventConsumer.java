package org.unisg.ftengrave.qcservice.adapter.in.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.adapter.in.kafka.dto.SortingMachineEventDto;
import org.unisg.ftengrave.qcservice.port.in.HandleColorDetectedEventUseCase;
import org.unisg.ftengrave.qcservice.port.in.HandleItemArrivedAtQcEventUseCase;
import org.unisg.ftengrave.qcservice.port.in.SortingMachineEvent;

@Component
@RequiredArgsConstructor
public class SortingMachineEventConsumer {

    private final HandleColorDetectedEventUseCase handleColorDetectedEventUseCase;
    private final HandleItemArrivedAtQcEventUseCase handleItemArrivedAtQcEventUseCase;

    @KafkaListener(topics = "${kafka.topic.sorting-machine}")
    public void consume(SortingMachineEventDto event) {
        SortingMachineEvent sortingMachineEvent = new SortingMachineEvent(event.getEventType(), event.getColor());
        handleItemArrivedAtQcEventUseCase.handle(sortingMachineEvent);
        handleColorDetectedEventUseCase.handle(sortingMachineEvent);
    }
}
