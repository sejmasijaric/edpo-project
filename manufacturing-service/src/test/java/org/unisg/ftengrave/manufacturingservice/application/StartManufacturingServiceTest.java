package org.unisg.ftengrave.manufacturingservice.application;

import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.unisg.ftengrave.manufacturingservice.port.out.CorrelateMessagePort;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StartManufacturingServiceTest {

    private final CorrelateMessagePort correlateMessagePort = Mockito.mock(CorrelateMessagePort.class);
    private final StartManufacturingService service = new StartManufacturingService(correlateMessagePort);

    @Test
    void startManufacturingReturnsTrueWhenMessageWasCorrelated() {
        MessageCorrelationResult result = Mockito.mock(MessageCorrelationResult.class);
        when(correlateMessagePort.correlateMessage(
                StartManufacturingService.START_MANUFACTURING_MESSAGE,
                "item-42",
                Map.of("itemIdentifier", "item-42")))
                .thenReturn(result);

        boolean started = service.startManufacturing("item-42");

        assertThat(started).isTrue();
        verify(correlateMessagePort).correlateMessage(
                StartManufacturingService.START_MANUFACTURING_MESSAGE,
                "item-42",
                Map.of("itemIdentifier", "item-42"));
    }

    @Test
    void startManufacturingReturnsFalseWhenNoMessageWasCorrelated() {
        when(correlateMessagePort.correlateMessage(
                StartManufacturingService.START_MANUFACTURING_MESSAGE,
                "item-42",
                Map.of("itemIdentifier", "item-42")))
                .thenReturn(null);

        assertThat(service.startManufacturing("item-42")).isFalse();
    }
}
