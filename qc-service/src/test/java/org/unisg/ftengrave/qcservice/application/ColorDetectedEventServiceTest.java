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
import org.unisg.ftengrave.qcservice.domain.ItemColor;
import org.unisg.ftengrave.qcservice.port.in.SortingMachineEvent;
import org.unisg.ftengrave.qcservice.port.out.CorrelateMessagePort;
import org.unisg.ftengrave.qcservice.port.out.ResolveWaitingMessageBusinessKeyPort;

@ExtendWith(MockitoExtension.class)
class ColorDetectedEventServiceTest {

    @Mock
    private ResolveWaitingMessageBusinessKeyPort resolveWaitingMessageBusinessKeyPort;

    @Mock
    private CorrelateMessagePort correlateMessagePort;

    private ColorDetectedEventService service;

    @BeforeEach
    void setUp() {
        service = new ColorDetectedEventService(resolveWaitingMessageBusinessKeyPort, correlateMessagePort);
    }

    @Test
    void correlatesDetectedColorRedEventForWaitingProcess() {
        when(resolveWaitingMessageBusinessKeyPort.resolve(ColorDetectedEventService.COLOR_DETECTED_MESSAGE)).thenReturn("item-42");

        service.handle(new SortingMachineEvent("color-detected", "red"));

        verify(correlateMessagePort).correlateMessage(
                eq(ColorDetectedEventService.COLOR_DETECTED_MESSAGE),
                eq("item-42"),
                eq(java.util.Map.of("itemIdentifier", "item-42", "detected-color", ItemColor.RED)));
    }

    @Test
    void correlatesNoneColorEventForBoundaryErrorHandling() {
        when(resolveWaitingMessageBusinessKeyPort.resolve(ColorDetectedEventService.COLOR_DETECTED_MESSAGE)).thenReturn("item-42");

        service.handle(new SortingMachineEvent("color-detected", "none"));

        verify(correlateMessagePort).correlateMessage(
                eq(ColorDetectedEventService.COLOR_DETECTED_MESSAGE),
                eq("item-42"),
                eq(java.util.Map.of("itemIdentifier", "item-42", "detected-color", ItemColor.NONE)));
    }

    @Test
    void ignoresNonColorEvents() {
        service.handle(new SortingMachineEvent("sort-to-shipping", null));

        verify(correlateMessagePort, never()).correlateMessage(any(), any(), any());
        verify(resolveWaitingMessageBusinessKeyPort, never()).resolve(any());
    }

    @Test
    void ignoresDetectedColorEventWhenNoProcessWaitsForMessage() {
        when(resolveWaitingMessageBusinessKeyPort.resolve(ColorDetectedEventService.COLOR_DETECTED_MESSAGE)).thenReturn(null);

        service.handle(new SortingMachineEvent("color-detected", "blue"));

        verify(correlateMessagePort, never()).correlateMessage(any(), any(), any());
    }

    @Test
    void ignoresColorDetectedEventWithoutColorPayload() {
        service.handle(new SortingMachineEvent("color-detected", null));

        verify(correlateMessagePort, never()).correlateMessage(any(), any(), any());
        verify(resolveWaitingMessageBusinessKeyPort, never()).resolve(any());
    }
}
