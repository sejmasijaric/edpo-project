package org.unisg.ftengrave.intakeservice.application;

import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.unisg.ftengrave.intakeservice.domain.ItemColor;
import org.unisg.ftengrave.intakeservice.port.out.CorrelateMessagePort;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class StartIntakeServiceTest {

    private final CorrelateMessagePort correlateMessagePort = Mockito.mock(CorrelateMessagePort.class);
    private final StartIntakeService service = new StartIntakeService(correlateMessagePort);

    @Test
    void startIntakeReturnsTrueWhenMessageWasCorrelated() {
        MessageCorrelationResult result = Mockito.mock(MessageCorrelationResult.class);
        Map<String, Object> expectedVariables = Map.of(
                "itemIdentifier", "item-42",
                "targetColor", ItemColor.RED,
                "itemArrivedAtIntake", false,
                "itemLeftIntake", false,
                "itemArrivedAtEngraver", false);
        when(correlateMessagePort.correlateMessage(StartIntakeService.START_INTAKE_MESSAGE, "item-42", expectedVariables))
                .thenReturn(result);

        boolean started = service.startIntake("item-42", ItemColor.RED);

        assertThat(started).isTrue();
        verify(correlateMessagePort).correlateMessage(StartIntakeService.START_INTAKE_MESSAGE, "item-42", expectedVariables);
    }

    @Test
    void startIntakeReturnsFalseWhenNoMessageWasCorrelated() {
        Map<String, Object> expectedVariables = Map.of(
                "itemIdentifier", "item-42",
                "targetColor", ItemColor.BLUE,
                "itemArrivedAtIntake", false,
                "itemLeftIntake", false,
                "itemArrivedAtEngraver", false);
        when(correlateMessagePort.correlateMessage(StartIntakeService.START_INTAKE_MESSAGE, "item-42", expectedVariables))
                .thenReturn(null);

        assertThat(service.startIntake("item-42", ItemColor.BLUE)).isFalse();
    }
}
