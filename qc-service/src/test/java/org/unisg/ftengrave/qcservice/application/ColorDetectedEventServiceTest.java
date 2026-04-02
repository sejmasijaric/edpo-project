package org.unisg.ftengrave.qcservice.application;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
    private WaitingMessageBusinessKeyResolver waitingMessageBusinessKeyResolver;

    @Mock
    private MessageCorrelationService messageCorrelationService;

    private ColorDetectedEventService service;

    @BeforeEach
    void setUp() {
        service = new ColorDetectedEventService(waitingMessageBusinessKeyResolver, messageCorrelationService);
    }

    @Test
    void correlatesDetectedColorRedEventForWaitingProcess() {
        when(waitingMessageBusinessKeyResolver.resolve(ColorDetectedEventService.COLOR_DETECTED_MESSAGE)).thenReturn("item-42");

        service.handle(new SortingMachineEventDto("color-detected", "red"));

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
        verify(waitingMessageBusinessKeyResolver, never()).resolve(any());
    }

    @Test
    void ignoresDetectedColorEventWhenNoProcessWaitsForMessage() {
        when(waitingMessageBusinessKeyResolver.resolve(ColorDetectedEventService.COLOR_DETECTED_MESSAGE)).thenReturn(null);

        service.handle(new SortingMachineEventDto("color-detected", "blue"));

        verify(messageCorrelationService, never()).correlateMessage(any(), any());
    }

    @Test
    void ignoresColorDetectedEventWithoutColorPayload() {
        service.handle(new SortingMachineEventDto("color-detected"));

        verify(messageCorrelationService, never()).correlateMessage(any(), any());
        verify(waitingMessageBusinessKeyResolver, never()).resolve(any());
    }
}
