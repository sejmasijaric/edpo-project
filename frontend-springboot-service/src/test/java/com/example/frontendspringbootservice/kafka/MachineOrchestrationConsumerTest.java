package com.example.frontendspringbootservice.kafka;

import com.example.frontendspringbootservice.dto.MachineOrchestrationEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@ExtendWith(MockitoExtension.class)
class MachineOrchestrationConsumerTest {

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    private MachineOrchestrationConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new MachineOrchestrationConsumer(messagingTemplate);
    }

    @Test
    void consume_forwardsEventToWebSocket() {
        var event = new MachineOrchestrationEvent("item-123", "intake-completed");

        consumer.consume(event);

        verify(messagingTemplate).convertAndSend("/topic/order-updates", event);
    }

    @Test
    void consume_forwardsManufacturingCompleted() {
        var event = new MachineOrchestrationEvent("item-456", "manufacturing-completed");

        consumer.consume(event);

        verify(messagingTemplate).convertAndSend("/topic/order-updates", event);
    }

    @Test
    void consume_forwardsManufacturingFailed() {
        var event = new MachineOrchestrationEvent("item-789", "manufacturing-failed");

        consumer.consume(event);

        verify(messagingTemplate).convertAndSend("/topic/order-updates", event);
    }

    @Test
    void consume_forwardsQcShipping() {
        var event = new MachineOrchestrationEvent("item-abc", "qc-shipping");

        consumer.consume(event);

        verify(messagingTemplate).convertAndSend("/topic/order-updates", event);
    }

    @Test
    void consume_forwardsQcRejection() {
        var event = new MachineOrchestrationEvent("item-def", "qc-rejection");

        consumer.consume(event);

        verify(messagingTemplate).convertAndSend("/topic/order-updates", event);
    }

    @Test
    void consume_preservesItemIdentifierAndOutcomeType() {
        var event = new MachineOrchestrationEvent("uuid-12345", "intake-completed");

        consumer.consume(event);

        ArgumentCaptor<MachineOrchestrationEvent> captor =
                ArgumentCaptor.forClass(MachineOrchestrationEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/order-updates"), captor.capture());

        MachineOrchestrationEvent forwarded = captor.getValue();
        assertThat(forwarded.itemIdentifier()).isEqualTo("uuid-12345");
        assertThat(forwarded.outcomeType()).isEqualTo("intake-completed");
    }

    @Test
    void consume_sendsToCorrectDestination() {
        var event = new MachineOrchestrationEvent("item-1", "intake-completed");

        consumer.consume(event);

        verify(messagingTemplate).convertAndSend(eq("/topic/order-updates"), eq(event));
        verifyNoMoreInteractions(messagingTemplate);
    }
}
