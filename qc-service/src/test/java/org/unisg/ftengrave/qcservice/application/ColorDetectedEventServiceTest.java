package org.unisg.ftengrave.qcservice.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ExecutionQuery;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstanceQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.unisg.ftengrave.qcservice.adapter.in.kafka.dto.SortingMachineEventDto;
import org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.MessageCorrelationService;
import org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.dto.CamundaMessageDto;
import org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.dto.ColorDetectedMessageProcessDto;
import org.unisg.ftengrave.qcservice.domain.ItemColor;

@ExtendWith(MockitoExtension.class)
class ColorDetectedEventServiceTest {

    @Mock
    private RuntimeService runtimeService;

    @Mock
    private MessageCorrelationService messageCorrelationService;

    @Mock
    private ExecutionQuery executionQuery;

    @Mock
    private ProcessInstanceQuery processInstanceQuery;

    @Mock
    private Execution execution;

    @Mock
    private ProcessInstance processInstance;

    private ColorDetectedEventService service;

    @BeforeEach
    void setUp() {
        service = new ColorDetectedEventService(runtimeService, messageCorrelationService);
    }

    @Test
    void correlatesDetectedColorRedEventForWaitingProcess() {
        when(runtimeService.createExecutionQuery()).thenReturn(executionQuery);
        when(executionQuery.messageEventSubscriptionName(ColorDetectedEventService.COLOR_DETECTED_MESSAGE)).thenReturn(executionQuery);
        when(executionQuery.singleResult()).thenReturn(execution);
        when(execution.getProcessInstanceId()).thenReturn("process-1");
        when(runtimeService.createProcessInstanceQuery()).thenReturn(processInstanceQuery);
        when(processInstanceQuery.processInstanceId("process-1")).thenReturn(processInstanceQuery);
        when(processInstanceQuery.singleResult()).thenReturn(processInstance);
        when(processInstance.getBusinessKey()).thenReturn("item-42");

        service.handle(new SortingMachineEventDto("detected-color-red"));

        CamundaMessageDto expectedMessage = CamundaMessageDto.builder()
                .dto(ColorDetectedMessageProcessDto.builder()
                        .itemIdentifier("item-42")
                        .color(ItemColor.RED)
                        .build())
                .build();
        verify(messageCorrelationService).correlateMessage(eq(expectedMessage), eq(ColorDetectedEventService.COLOR_DETECTED_MESSAGE));
    }

    @Test
    void ignoresNonColorEvents() {
        service.handle(new SortingMachineEventDto("sort-to-shipping"));

        verify(messageCorrelationService, never()).correlateMessage(any(), any());
        verify(runtimeService, never()).createExecutionQuery();
    }

    @Test
    void ignoresDetectedColorEventWhenNoProcessWaitsForMessage() {
        when(runtimeService.createExecutionQuery()).thenReturn(executionQuery);
        when(executionQuery.messageEventSubscriptionName(ColorDetectedEventService.COLOR_DETECTED_MESSAGE)).thenReturn(executionQuery);
        when(executionQuery.singleResult()).thenReturn(null);

        service.handle(new SortingMachineEventDto("detected-color-blue"));

        verify(messageCorrelationService, never()).correlateMessage(any(), any());
    }
}
