package org.unisg.ftengrave.orderorchestrator.application;

import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.orderorchestrator.port.out.CorrelateMessagePort;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StartOrderOrchestrationServiceTest {

    @Test
    void startOrderOrchestrationCorrelatesStartMessage() {
        RecordingCorrelateMessagePort correlateMessagePort = new RecordingCorrelateMessagePort();
        StartOrderOrchestrationService service = new StartOrderOrchestrationService(correlateMessagePort);

        boolean started = service.startOrderOrchestration("order-42");

        assertThat(started).isTrue();
        assertThat(correlateMessagePort.messageName).isEqualTo("StartOrderOrchestrationMessage");
        assertThat(correlateMessagePort.orderIdentifier).isEqualTo("order-42");
        assertThat(correlateMessagePort.variables).isEqualTo(Map.of("orderIdentifier", "order-42"));
    }

    private static final class RecordingCorrelateMessagePort implements CorrelateMessagePort {

        private String messageName;
        private String orderIdentifier;
        private Map<String, Object> variables;

        @Override
        public MessageCorrelationResult correlateMessage(String messageName, String orderIdentifier, Map<String, Object> variables) {
            this.messageName = messageName;
            this.orderIdentifier = orderIdentifier;
            this.variables = variables;
            return org.mockito.Mockito.mock(MessageCorrelationResult.class);
        }
    }
}
