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
import org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.dto.CamundaMessageDto;
import org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.dto.ColorDetectedMessageProcessDto;
import org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.dto.MessageProcessDto;
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
        CamundaMessageDto messageDto = CamundaMessageDto.builder()
                .dto(MessageProcessDto.builder()
                        .itemIdentifier("item-42")
                        .targetColor(ItemColor.RED)
                        .build())
                .build();

        when(runtimeService.createMessageCorrelation("MessageKafkaDemo")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariables(Map.of("itemIdentifier", "item-42", "targetColor", "RED"))).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.processInstanceBusinessKey("item-42")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateWithResult()).thenReturn(messageCorrelationResult);

        MessageCorrelationResult result = messageCorrelationService.correlateMessage(messageDto, "MessageKafkaDemo");

        assertThat(result).isSameAs(messageCorrelationResult);
        verify(messageCorrelationBuilder).processInstanceBusinessKey("item-42");
        verify(messageCorrelationBuilder).setVariables(Map.of("itemIdentifier", "item-42", "targetColor", "RED"));
    }

    @Test
    void correlatesUsingGenericPayloadMap() {
        CamundaMessageDto messageDto = CamundaMessageDto.builder()
                .dto(Map.of("itemIdentifier", "item-99", "qualityStatus", "PASSED"))
                .build();

        when(runtimeService.createMessageCorrelation("MessageKafkaDemo")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariables(Map.of("itemIdentifier", "item-99", "qualityStatus", "PASSED"))).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.processInstanceBusinessKey("item-99")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateWithResult()).thenReturn(messageCorrelationResult);

        MessageCorrelationResult result = messageCorrelationService.correlateMessage(messageDto, "MessageKafkaDemo");

        assertThat(result).isSameAs(messageCorrelationResult);
        verify(messageCorrelationBuilder).setVariables(Map.of("itemIdentifier", "item-99", "qualityStatus", "PASSED"));
        verify(messageCorrelationBuilder).processInstanceBusinessKey("item-99");
    }

    @Test
    void correlatesColorDetectedMessageIncludingColorVariable() {
        CamundaMessageDto messageDto = CamundaMessageDto.builder()
                .dto(ColorDetectedMessageProcessDto.builder()
                        .itemIdentifier("item-77")
                        .color(ItemColor.BLUE)
                        .build())
                .build();

        when(runtimeService.createMessageCorrelation("ColorDetectedMessage")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariables(Map.of("itemIdentifier", "item-77", "detected-color", "BLUE"))).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.processInstanceBusinessKey("item-77")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateWithResult()).thenReturn(messageCorrelationResult);

        MessageCorrelationResult result = messageCorrelationService.correlateMessage(messageDto, "ColorDetectedMessage");

        assertThat(result).isSameAs(messageCorrelationResult);
        verify(messageCorrelationBuilder).setVariables(Map.of("itemIdentifier", "item-77", "detected-color", "BLUE"));
        verify(messageCorrelationBuilder).processInstanceBusinessKey("item-77");
    }

    @Test
    void omitsUnsetDtoFieldsBeforePassingVariablesToCamunda() {
        CamundaMessageDto messageDto = CamundaMessageDto.builder()
                .dto(MessageProcessDto.builder()
                        .itemIdentifier("item-42")
                        .build())
                .build();

        when(runtimeService.createMessageCorrelation("ItemArrivedAtQC")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariables(Map.of("itemIdentifier", "item-42"))).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.processInstanceBusinessKey("item-42")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateWithResult()).thenReturn(messageCorrelationResult);

        MessageCorrelationResult result = messageCorrelationService.correlateMessage(messageDto, "ItemArrivedAtQC");

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

        CamundaMessageDto messageDto = CamundaMessageDto.builder()
                .dto(payload)
                .build();

        when(runtimeService.createMessageCorrelation("MessageKafkaDemo")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariables(Map.of("itemIdentifier", "item-99", "qualityStatus", "PASSED")))
                .thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.processInstanceBusinessKey("item-99")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateWithResult()).thenReturn(messageCorrelationResult);

        MessageCorrelationResult result = messageCorrelationService.correlateMessage(messageDto, "MessageKafkaDemo");

        assertThat(result).isSameAs(messageCorrelationResult);
        verify(messageCorrelationBuilder).setVariables(Map.of("itemIdentifier", "item-99", "qualityStatus", "PASSED"));
        verify(messageCorrelationBuilder).processInstanceBusinessKey("item-99");
    }
}
