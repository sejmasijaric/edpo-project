package org.unisg.ftengrave.intakeservice.adapter.in.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.unisg.ftengrave.intakeservice.adapter.in.kafka.dto.VacuumGripperEventDto;
import org.unisg.ftengrave.intakeservice.port.in.HandleItemArrivedAtIntakeEventUseCase;
import org.unisg.ftengrave.intakeservice.port.in.HandleItemLeftIntakeEventUseCase;
import org.unisg.ftengrave.intakeservice.port.in.VacuumGripperEvent;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class VacuumGripperEventConsumerTest {

    @Mock
    private HandleItemArrivedAtIntakeEventUseCase handleItemArrivedAtIntakeEventUseCase;

    @Mock
    private HandleItemLeftIntakeEventUseCase handleItemLeftIntakeEventUseCase;

    @InjectMocks
    private VacuumGripperEventConsumer consumer;

    @Test
    void delegatesConsumedEventToApplicationServices() {
        consumer.consume(new VacuumGripperEventDto("item-left-intake"));

        verify(handleItemArrivedAtIntakeEventUseCase).handle(new VacuumGripperEvent("item-left-intake"));
        verify(handleItemLeftIntakeEventUseCase).handle(new VacuumGripperEvent("item-left-intake"));
    }
}
