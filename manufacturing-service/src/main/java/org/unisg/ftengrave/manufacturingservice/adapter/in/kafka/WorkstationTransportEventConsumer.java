package org.unisg.ftengrave.manufacturingservice.adapter.in.kafka;

import lombok.RequiredArgsConstructor;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.manufacturingservice.adapter.in.kafka.dto.WorkstationTransportEventDto;
import org.unisg.ftengrave.manufacturingservice.port.in.HandleWtMoveCompletedEventUseCase;
import org.unisg.ftengrave.manufacturingservice.port.in.WorkstationTransportEvent;

@Component
@RequiredArgsConstructor
public class WorkstationTransportEventConsumer {

    private final HandleWtMoveCompletedEventUseCase handleWtMoveCompletedEventUseCase;

    @KafkaListener(
            topics = "${kafka.topic.workstation-transport}",
            containerFactory = "workstationTransportEventKafkaListenerContainerFactory")
    public void consume(WorkstationTransportEventDto event) {
        handleWtMoveCompletedEventUseCase.handle(
                new WorkstationTransportEvent(event == null ? null : event.getEventType()));
    }
}
