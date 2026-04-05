package org.unisg.ftengrave.qcservice.adapter.in.kafka;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.unisg.ftengrave.qcservice.adapter.in.kafka.dto.SortingMachineEventDto;
import org.unisg.ftengrave.qcservice.port.in.HandleColorDetectedEventUseCase;
import org.unisg.ftengrave.qcservice.port.in.HandleItemArrivedAtQcEventUseCase;
import org.unisg.ftengrave.qcservice.port.in.SortingMachineEvent;

@ExtendWith(MockitoExtension.class)
class SortingMachineEventConsumerTest {

    @Mock
    private HandleColorDetectedEventUseCase handleColorDetectedEventUseCase;

    @Mock
    private HandleItemArrivedAtQcEventUseCase handleItemArrivedAtQcEventUseCase;

    @InjectMocks
    private SortingMachineEventConsumer consumer;

    @Test
    void delegatesConsumedEventToApplicationService() {
        SortingMachineEventDto event = new SortingMachineEventDto("color-detected", "white");

        consumer.consume(event);

        verify(handleItemArrivedAtQcEventUseCase).handle(new SortingMachineEvent("color-detected", "white"));
        verify(handleColorDetectedEventUseCase).handle(new SortingMachineEvent("color-detected", "white"));
    }
}
