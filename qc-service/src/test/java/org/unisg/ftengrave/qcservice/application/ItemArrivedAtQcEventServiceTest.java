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
import org.unisg.ftengrave.qcservice.port.in.SortingMachineEvent;
import org.unisg.ftengrave.qcservice.port.out.CorrelateMessagePort;
import org.unisg.ftengrave.qcservice.port.out.ResolveWaitingMessageBusinessKeyPort;

@ExtendWith(MockitoExtension.class)
class ItemArrivedAtQcEventServiceTest {

    @Mock
    private ResolveWaitingMessageBusinessKeyPort resolveWaitingMessageBusinessKeyPort;

    @Mock
    private CorrelateMessagePort correlateMessagePort;

    private ItemArrivedAtQcEventService service;

    @BeforeEach
    void setUp() {
        service = new ItemArrivedAtQcEventService(resolveWaitingMessageBusinessKeyPort, correlateMessagePort);
    }

    @Test
    void correlatesItemArrivedAtQcForWaitingProcess() {
        when(resolveWaitingMessageBusinessKeyPort.resolve(ItemArrivedAtQcEventService.ITEM_ARRIVED_AT_QC_MESSAGE))
                .thenReturn("item-42");

        service.handle(new SortingMachineEvent("item-arrived-at-qc", null));

        verify(correlateMessagePort).correlateMessage(
                eq(ItemArrivedAtQcEventService.ITEM_ARRIVED_AT_QC_MESSAGE),
                eq("item-42"),
                eq(java.util.Map.of("itemIdentifier", "item-42")));
    }

    @Test
    void ignoresUnrelatedSorterEvents() {
        service.handle(new SortingMachineEvent("color-detected", "blue"));

        verify(correlateMessagePort, never()).correlateMessage(any(), any(), any());
        verify(resolveWaitingMessageBusinessKeyPort, never()).resolve(any());
    }

    @Test
    void ignoresItemArrivedAtQcWhenNoProcessWaitsForMessage() {
        when(resolveWaitingMessageBusinessKeyPort.resolve(ItemArrivedAtQcEventService.ITEM_ARRIVED_AT_QC_MESSAGE))
                .thenReturn(null);

        service.handle(new SortingMachineEvent("item-arrived-at-qc", null));

        verify(correlateMessagePort, never()).correlateMessage(any(), any(), any());
    }
}
