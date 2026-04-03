package org.unisg.ftengrave.orderorchestrator.adapter.out.bpmn_messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageCorrelationServiceTest {

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private MessageCorrelationBuilder messageCorrelationBuilder;

    @Mock
    private MessageCorrelationResult messageCorrelationResult;

    private MessageCorrelationService messageCorrelationService;

    @BeforeEach
    void setUp() {
        messageCorrelationService = new MessageCorrelationService(runtimeService, new ObjectMapper());
    }

    @Test
    void correlatesUsingOrderIdentifierFromPayloadMap() {
        when(runtimeService.createMessageCorrelation("StartOrderOrchestrationMessage")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariables(Map.of("orderIdentifier", "order-42"))).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.processInstanceBusinessKey("order-42")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateWithResult()).thenReturn(messageCorrelationResult);

        MessageCorrelationResult result = messageCorrelationService.correlateMessage(
                "StartOrderOrchestrationMessage",
                "order-42",
                Map.of("orderIdentifier", "order-42"));

        assertThat(result).isSameAs(messageCorrelationResult);
        verify(messageCorrelationBuilder).processInstanceBusinessKey("order-42");
        verify(messageCorrelationBuilder).setVariables(Map.of("orderIdentifier", "order-42"));
    }

    @Test
    void omitsNullEntriesFromPayloadMaps() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("orderIdentifier", "order-99");
        payload.put("priority", null);
        payload.put("status", "CREATED");

        when(runtimeService.createMessageCorrelation("OrderUpdatedMessage")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariables(Map.of("orderIdentifier", "order-99", "status", "CREATED")))
                .thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.processInstanceBusinessKey("order-99")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateWithResult()).thenReturn(messageCorrelationResult);

        MessageCorrelationResult result = messageCorrelationService.correlateMessage(
                "OrderUpdatedMessage",
                "order-99",
                payload);

        assertThat(result).isSameAs(messageCorrelationResult);
        verify(messageCorrelationBuilder).setVariables(Map.of("orderIdentifier", "order-99", "status", "CREATED"));
        verify(messageCorrelationBuilder).processInstanceBusinessKey("order-99");
    }
}
