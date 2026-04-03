package org.unisg.ftengrave.orderorchestrator.application;

import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;
import org.unisg.ftengrave.orderorchestrator.port.out.CorrelateMessagePort;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class StartOrderOrchestrationServiceTest {

    @Test
    void startOrderOrchestrationCorrelatesOrderCreatedMessage() {
        RecordingCorrelateMessagePort correlateMessagePort = new RecordingCorrelateMessagePort();
        StartOrderOrchestrationService service = new StartOrderOrchestrationService(correlateMessagePort);

        boolean started = service.startOrderOrchestration("item-42", ItemColor.RED);

        assertThat(started).isTrue();
        assertThat(correlateMessagePort.messageName).isEqualTo("OrderCreatedMessage");
        assertThat(correlateMessagePort.businessKey).isEqualTo("item-42");
        assertThat(correlateMessagePort.variables).isEqualTo(Map.of(
                "itemIdentifier", "item-42",
                "targetColor", ItemColor.RED));
    }

    private static final class RecordingCorrelateMessagePort implements CorrelateMessagePort {

        private String messageName;
        private String businessKey;
        private Map<String, Object> variables;

        @Override
        public MessageCorrelationResult correlateMessage(String messageName, String businessKey, Map<String, Object> variables) {
            this.messageName = messageName;
            this.businessKey = businessKey;
            this.variables = variables;
            return org.mockito.Mockito.mock(MessageCorrelationResult.class);
        }
    }
}
