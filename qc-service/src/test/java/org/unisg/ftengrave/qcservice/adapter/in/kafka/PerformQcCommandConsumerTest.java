package org.unisg.ftengrave.qcservice.adapter.in.kafka;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.unisg.ftengrave.qcservice.adapter.in.kafka.dto.PerformQcCommandDto;
import org.unisg.ftengrave.qcservice.domain.ItemColor;
import org.unisg.ftengrave.qcservice.port.in.StartQcUseCase;

import static org.mockito.Mockito.verify;

class PerformQcCommandConsumerTest {

    @Test
    void consumeDelegatesToStartQcUseCase() {
        StartQcUseCase startQcUseCase = Mockito.mock(StartQcUseCase.class);
        PerformQcCommandConsumer consumer = new PerformQcCommandConsumer(startQcUseCase);

        consumer.consume(new PerformQcCommandDto("item-42", ItemColor.RED));

        verify(startQcUseCase).startQc("item-42", ItemColor.RED);
    }
}
