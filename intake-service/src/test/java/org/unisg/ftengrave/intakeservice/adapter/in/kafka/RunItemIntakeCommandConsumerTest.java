package org.unisg.ftengrave.intakeservice.adapter.in.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.unisg.ftengrave.intakeservice.adapter.in.kafka.dto.RunItemIntakeCommandDto;
import org.unisg.ftengrave.intakeservice.domain.ItemColor;
import org.unisg.ftengrave.intakeservice.port.in.StartIntakeUseCase;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RunItemIntakeCommandConsumerTest {

    @Mock
    private StartIntakeUseCase startIntakeUseCase;

    @InjectMocks
    private RunItemIntakeCommandConsumer consumer;

    @Test
    void delegatesMatchingCommandToApplicationService() {
        consumer.consume(new RunItemIntakeCommandDto("run-item-intake-command", "item-42", ItemColor.WHITE));

        verify(startIntakeUseCase).startIntake("item-42", ItemColor.WHITE);
    }

    @Test
    void ignoresOtherCommands() {
        consumer.consume(new RunItemIntakeCommandDto("run-item-qc-command", "item-42", ItemColor.WHITE));

        verify(startIntakeUseCase, never()).startIntake("item-42", ItemColor.WHITE);
    }
}
