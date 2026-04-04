package org.unisg.ftengrave.intakeservice.adapter.in.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.unisg.ftengrave.intakeservice.adapter.in.kafka.dto.EngraverEventDto;
import org.unisg.ftengrave.intakeservice.port.in.EngraverEvent;
import org.unisg.ftengrave.intakeservice.port.in.HandleItemArrivedAtEngraverEventUseCase;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class EngraverEventConsumerTest {

    @Mock
    private HandleItemArrivedAtEngraverEventUseCase handleItemArrivedAtEngraverEventUseCase;

    @InjectMocks
    private EngraverEventConsumer consumer;

    @Test
    void delegatesConsumedEventToApplicationService() {
        consumer.consume(new EngraverEventDto("item-arrived-at-engraver-sink"));

        verify(handleItemArrivedAtEngraverEventUseCase).handle(new EngraverEvent("item-arrived-at-engraver-sink"));
    }
}
