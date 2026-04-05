package org.unisg.ftengrave.manufacturingservice.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.unisg.ftengrave.manufacturingservice.port.in.EngraverEvent;
import org.unisg.ftengrave.manufacturingservice.port.in.PolishingMachineEvent;
import org.unisg.ftengrave.manufacturingservice.port.in.WorkstationTransportEvent;
import org.unisg.ftengrave.manufacturingservice.port.out.CorrelateMessagePort;
import org.unisg.ftengrave.manufacturingservice.port.out.ResolveWaitingMessageBusinessKeyPort;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ManufacturingEventServicesTest {

    @Mock
    private ResolveWaitingMessageBusinessKeyPort resolveWaitingMessageBusinessKeyPort;

    @Mock
    private CorrelateMessagePort correlateMessagePort;

    private ItemLeftEngraverSinkEventService itemLeftEngraverSinkEventService;
    private ItemArrivedAtEngraverSinkEventService itemArrivedAtEngraverSinkEventService;
    private WtMoveCompletedEventService wtMoveCompletedEventService;
    private ItemArrivedAtPolishingMachineOutputEventService itemArrivedAtPolishingMachineOutputEventService;

    @BeforeEach
    void setUp() {
        itemLeftEngraverSinkEventService = new ItemLeftEngraverSinkEventService(
                resolveWaitingMessageBusinessKeyPort, correlateMessagePort);
        itemArrivedAtEngraverSinkEventService = new ItemArrivedAtEngraverSinkEventService(
                resolveWaitingMessageBusinessKeyPort, correlateMessagePort);
        wtMoveCompletedEventService = new WtMoveCompletedEventService(
                resolveWaitingMessageBusinessKeyPort, correlateMessagePort);
        itemArrivedAtPolishingMachineOutputEventService = new ItemArrivedAtPolishingMachineOutputEventService(
                resolveWaitingMessageBusinessKeyPort, correlateMessagePort);
    }

    @Test
    void correlatesItemLeftEngraverSinkForWaitingProcess() {
        when(resolveWaitingMessageBusinessKeyPort.resolve(ItemLeftEngraverSinkEventService.ITEM_LEFT_ENGRAVER_SINK_MESSAGE))
                .thenReturn("item-42");

        itemLeftEngraverSinkEventService.handle(new EngraverEvent("item-left-engraver-sink"));

        verify(correlateMessagePort).correlateMessage(
                eq(ItemLeftEngraverSinkEventService.ITEM_LEFT_ENGRAVER_SINK_MESSAGE),
                eq("item-42"),
                eq(Map.of("itemIdentifier", "item-42")));
    }

    @Test
    void correlatesItemArrivedAtEngraverSinkForWaitingProcess() {
        when(resolveWaitingMessageBusinessKeyPort.resolve(ItemArrivedAtEngraverSinkEventService.ITEM_ARRIVED_AT_ENGRAVER_SINK_MESSAGE))
                .thenReturn("item-42");

        itemArrivedAtEngraverSinkEventService.handle(new EngraverEvent("item-arrived-at-engraver-sink"));

        verify(correlateMessagePort).correlateMessage(
                eq(ItemArrivedAtEngraverSinkEventService.ITEM_ARRIVED_AT_ENGRAVER_SINK_MESSAGE),
                eq("item-42"),
                eq(Map.of("itemIdentifier", "item-42")));
    }

    @Test
    void correlatesWtMoveCompletedForWaitingProcess() {
        when(resolveWaitingMessageBusinessKeyPort.resolve(WtMoveCompletedEventService.WT_MOVE_COMPLETED_MESSAGE))
                .thenReturn("item-42");

        wtMoveCompletedEventService.handle(new WorkstationTransportEvent("wt-move-completed"));

        verify(correlateMessagePort).correlateMessage(
                eq(WtMoveCompletedEventService.WT_MOVE_COMPLETED_MESSAGE),
                eq("item-42"),
                eq(Map.of("itemIdentifier", "item-42")));
    }

    @Test
    void correlatesItemArrivedAtPolishingMachineOutputForWaitingProcess() {
        when(resolveWaitingMessageBusinessKeyPort.resolve(
                ItemArrivedAtPolishingMachineOutputEventService.ITEM_ARRIVED_AT_POLISHING_MACHINE_OUTPUT_MESSAGE))
                .thenReturn("item-42");

        itemArrivedAtPolishingMachineOutputEventService.handle(
                new PolishingMachineEvent("item-arrived-at-polishing-machine-output"));

        verify(correlateMessagePort).correlateMessage(
                eq(ItemArrivedAtPolishingMachineOutputEventService.ITEM_ARRIVED_AT_POLISHING_MACHINE_OUTPUT_MESSAGE),
                eq("item-42"),
                eq(Map.of("itemIdentifier", "item-42")));
    }

    @Test
    void ignoresUnrelatedEvents() {
        itemLeftEngraverSinkEventService.handle(new EngraverEvent("item-arrived-at-engraver-sink"));
        itemArrivedAtEngraverSinkEventService.handle(new EngraverEvent("item-left-engraver-sink"));
        wtMoveCompletedEventService.handle(new WorkstationTransportEvent("wt-other"));
        itemArrivedAtPolishingMachineOutputEventService.handle(new PolishingMachineEvent("pm-other"));

        verify(correlateMessagePort, never()).correlateMessage(any(), any(), any());
        verify(resolveWaitingMessageBusinessKeyPort, never()).resolve(any());
    }
}
