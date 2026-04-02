package org.unisg.ftengrave.qcservice.adapter.in.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.adapter.in.kafka.dto.SortingMachineEventDto;
import org.unisg.ftengrave.qcservice.application.ColorDetectedEventService;
import org.unisg.ftengrave.qcservice.application.ItemArrivedAtQcEventService;

@Component
@RequiredArgsConstructor
public class SortingMachineEventConsumer {

    private final ColorDetectedEventService colorDetectedEventService;
    private final ItemArrivedAtQcEventService itemArrivedAtQcEventService;

    @KafkaListener(topics = "${kafka.topic.sorting-machine}")
    public void consume(SortingMachineEventDto event) {
        itemArrivedAtQcEventService.handle(event);
        colorDetectedEventService.handle(event);
    }
}
