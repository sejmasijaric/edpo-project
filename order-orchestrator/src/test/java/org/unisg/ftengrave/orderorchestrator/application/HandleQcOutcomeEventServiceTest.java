package org.unisg.ftengrave.orderorchestrator.application;

import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.orderorchestrator.port.out.CorrelateMessagePort;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HandleQcOutcomeEventServiceTest {

    @Test
    void shippingOutcomeCorrelatesShippingMessage() {
        RecordingCorrelateMessagePort port = new RecordingCorrelateMessagePort();

        new HandleQcOutcomeEventService(port).handle("item-42", "qc-shipping");

        assertThat(port.messageName).isEqualTo("QcShippingOutcomeMessage");
        assertThat(port.businessKey).isEqualTo("item-42");
        assertThat(port.variables).isNull();
    }

    @Test
    void rejectionOutcomeCorrelatesRejectionMessage() {
        RecordingCorrelateMessagePort port = new RecordingCorrelateMessagePort();

        new HandleQcOutcomeEventService(port).handle("item-42", "qc-rejection");

        assertThat(port.messageName).isEqualTo("QcRejectionOutcomeMessage");
        assertThat(port.businessKey).isEqualTo("item-42");
        assertThat(port.variables).isNull();
    }

    @Test
    void unknownOutcomeIsIgnored() {
        RecordingCorrelateMessagePort port = new RecordingCorrelateMessagePort();

        new HandleQcOutcomeEventService(port).handle("item-42", "unknown");

        assertThat(port.messageName).isNull();
    }

    private static final class RecordingCorrelateMessagePort implements CorrelateMessagePort {

        private String messageName;
        private String businessKey;
        private Map<String, Object> variables;

        @Override
        public MessageCorrelationResult correlateMessage(String messageName, String orderIdentifier, Map<String, Object> variables) {
            this.messageName = messageName;
            this.businessKey = orderIdentifier;
            this.variables = variables;
            return null;
        }
    }
}
