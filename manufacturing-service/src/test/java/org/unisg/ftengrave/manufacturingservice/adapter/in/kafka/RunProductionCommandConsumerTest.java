package org.unisg.ftengrave.manufacturingservice.adapter.in.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.unisg.ftengrave.manufacturingservice.adapter.in.kafka.dto.RunProductionCommandDto;
import org.unisg.ftengrave.manufacturingservice.port.in.StartManufacturingUseCase;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RunProductionCommandConsumerTest {

    @Mock
    private StartManufacturingUseCase startManufacturingUseCase;

    @InjectMocks
    private RunProductionCommandConsumer consumer;

    @Test
    void delegatesMatchingCommandToApplicationService() {
        consumer.consume(new RunProductionCommandDto("run-production-command", "item-42"));

        verify(startManufacturingUseCase).startManufacturing("item-42");
    }

    @Test
    void ignoresOtherCommands() {
        consumer.consume(new RunProductionCommandDto("run-other-command", "item-42"));

        verify(startManufacturingUseCase, never()).startManufacturing("item-42");
    }
}
