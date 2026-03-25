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
import org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.dto.MessageProcessDto;

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
                        .build())
                .build();

        when(runtimeService.createMessageCorrelation("MessageKafkaDemo")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.setVariables(Map.of("itemIdentifier", "item-42"))).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.processInstanceBusinessKey("item-42")).thenReturn(messageCorrelationBuilder);
        when(messageCorrelationBuilder.correlateWithResult()).thenReturn(messageCorrelationResult);

        MessageCorrelationResult result = messageCorrelationService.correlateMessage(messageDto, "MessageKafkaDemo");

        assertThat(result).isSameAs(messageCorrelationResult);
        verify(messageCorrelationBuilder).processInstanceBusinessKey("item-42");
        verify(messageCorrelationBuilder).setVariables(Map.of("itemIdentifier", "item-42"));
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
}
