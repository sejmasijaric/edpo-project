package org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.unisg.ftengrave.qcservice.domain.ItemColor;

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
    void correlatesUsingItemIdentifierFromSampleDto() {
        when(runtimeService.createMessageCorrelation("MessageKafkaDemo")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariables(Map.of("itemIdentifier", "item-42", "targetColor", "RED"))).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.processInstanceBusinessKey("item-42")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateWithResult()).thenReturn(messageCorrelationResult);

        MessageCorrelationResult result = messageCorrelationService.correlateMessage(
                "MessageKafkaDemo",
                "item-42",
                Map.of("itemIdentifier", "item-42", "targetColor", ItemColor.RED));

        assertThat(result).isSameAs(messageCorrelationResult);
        verify(messageCorrelationBuilder).processInstanceBusinessKey("item-42");
        verify(messageCorrelationBuilder).setVariables(Map.of("itemIdentifier", "item-42", "targetColor", "RED"));
    }

    @Test
    void correlatesUsingGenericPayloadMap() {
        when(runtimeService.createMessageCorrelation("MessageKafkaDemo")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariables(Map.of("itemIdentifier", "item-99", "qualityStatus", "PASSED"))).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.processInstanceBusinessKey("item-99")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateWithResult()).thenReturn(messageCorrelationResult);

        MessageCorrelationResult result = messageCorrelationService.correlateMessage(
                "MessageKafkaDemo",
                "item-99",
                Map.of("itemIdentifier", "item-99", "qualityStatus", "PASSED"));

        assertThat(result).isSameAs(messageCorrelationResult);
        verify(messageCorrelationBuilder).setVariables(Map.of("itemIdentifier", "item-99", "qualityStatus", "PASSED"));
        verify(messageCorrelationBuilder).processInstanceBusinessKey("item-99");
    }

    @Test
    void correlatesColorDetectedMessageIncludingColorVariable() {
        when(runtimeService.createMessageCorrelation("ColorDetectedMessage")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariables(Map.of("itemIdentifier", "item-77", "detected-color", "BLUE"))).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.processInstanceBusinessKey("item-77")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateWithResult()).thenReturn(messageCorrelationResult);

        MessageCorrelationResult result = messageCorrelationService.correlateMessage(
                "ColorDetectedMessage",
                "item-77",
                Map.of("itemIdentifier", "item-77", "detected-color", ItemColor.BLUE));

        assertThat(result).isSameAs(messageCorrelationResult);
        verify(messageCorrelationBuilder).setVariables(Map.of("itemIdentifier", "item-77", "detected-color", "BLUE"));
        verify(messageCorrelationBuilder).processInstanceBusinessKey("item-77");
    }

    @Test
    void omitsUnsetDtoFieldsBeforePassingVariablesToCamunda() {
        when(runtimeService.createMessageCorrelation("ItemArrivedAtQC")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariables(Map.of("itemIdentifier", "item-42"))).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.processInstanceBusinessKey("item-42")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateWithResult()).thenReturn(messageCorrelationResult);

        MessageCorrelationResult result = messageCorrelationService.correlateMessage(
                "ItemArrivedAtQC",
                "item-42",
                Map.of("itemIdentifier", "item-42"));

        assertThat(result).isSameAs(messageCorrelationResult);
        verify(messageCorrelationBuilder).setVariables(Map.of("itemIdentifier", "item-42"));
        verify(messageCorrelationBuilder).processInstanceBusinessKey("item-42");
    }

    @Test
    void omitsNullEntriesFromGenericPayloadMaps() {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("itemIdentifier", "item-99");
        payload.put("targetColor", null);
        payload.put("qualityStatus", "PASSED");

        when(runtimeService.createMessageCorrelation("MessageKafkaDemo")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariables(Map.of("itemIdentifier", "item-99", "qualityStatus", "PASSED")))
                .thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.processInstanceBusinessKey("item-99")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateWithResult()).thenReturn(messageCorrelationResult);

        MessageCorrelationResult result = messageCorrelationService.correlateMessage(
                "MessageKafkaDemo",
                "item-99",
                payload);

        assertThat(result).isSameAs(messageCorrelationResult);
        verify(messageCorrelationBuilder).setVariables(Map.of("itemIdentifier", "item-99", "qualityStatus", "PASSED"));
        verify(messageCorrelationBuilder).processInstanceBusinessKey("item-99");
    }
}
