package org.unisg.ftengrave.qcservice.adapter.in.kafka;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.unisg.ftengrave.qcservice.adapter.in.kafka.dto.SortingMachineEventDto;
import org.unisg.ftengrave.qcservice.application.ColorDetectedEventService;
import org.unisg.ftengrave.qcservice.application.ItemArrivedAtQcEventService;

@ExtendWith(MockitoExtension.class)
class SortingMachineEventConsumerTest {

    @Mock
    private ColorDetectedEventService colorDetectedEventService;

    @Mock
    private ItemArrivedAtQcEventService itemArrivedAtQcEventService;

    @InjectMocks
    private SortingMachineEventConsumer consumer;

    @Test
    void delegatesConsumedEventToApplicationService() {
        SortingMachineEventDto event = new SortingMachineEventDto("color-detected", "white");

        consumer.consume(event);

        verify(itemArrivedAtQcEventService).handle(event);
        verify(colorDetectedEventService).handle(event);
    }
}
