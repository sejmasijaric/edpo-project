package org.unisg.ftengrave.qcservice.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.unisg.ftengrave.qcservice.domain.ItemColor;
import org.unisg.ftengrave.qcservice.port.out.CorrelateMessagePort;

import java.util.Map;

class StartQcServiceTest {

    private final CorrelateMessagePort correlateMessagePort = Mockito.mock(CorrelateMessagePort.class);
    private final StartQcService service = new StartQcService(correlateMessagePort);

    @Test
    void startQcReturnsTrueWhenMessageWasCorrelated() {
        MessageCorrelationResult result = Mockito.mock(MessageCorrelationResult.class);
        when(correlateMessagePort.correlateMessage(
                StartQcService.START_QC_MESSAGE,
                "item-42",
                Map.of("itemIdentifier", "item-42", "targetColor", ItemColor.RED)))
                .thenReturn(result);

        boolean started = service.startQc("item-42", ItemColor.RED);

        assertThat(started).isTrue();
        verify(correlateMessagePort).correlateMessage(
                StartQcService.START_QC_MESSAGE,
                "item-42",
                Map.of("itemIdentifier", "item-42", "targetColor", ItemColor.RED));
    }

    @Test
    void startQcReturnsFalseWhenNoMessageWasCorrelated() {
        when(correlateMessagePort.correlateMessage(
                StartQcService.START_QC_MESSAGE,
                "item-42",
                Map.of("itemIdentifier", "item-42", "targetColor", ItemColor.BLUE)))
                .thenReturn(null);

        assertThat(service.startQc("item-42", ItemColor.BLUE)).isFalse();
    }
}
