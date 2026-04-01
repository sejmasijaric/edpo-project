package org.unisg.ftengrave.qcservice.adapter.in.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.adapter.in.kafka.dto.SortingMachineEventDto;
import org.unisg.ftengrave.qcservice.application.ColorDetectedEventService;

@Component
@RequiredArgsConstructor
public class SortingMachineEventConsumer {

    private final ColorDetectedEventService colorDetectedEventService;

    @KafkaListener(topics = "${kafka.topic.sorting-machine}")
    public void consume(SortingMachineEventDto event) {
        colorDetectedEventService.handle(event);
    }
}
