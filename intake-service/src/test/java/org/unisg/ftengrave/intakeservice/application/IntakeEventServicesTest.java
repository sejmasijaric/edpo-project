package org.unisg.ftengrave.intakeservice.application;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.unisg.ftengrave.intakeservice.port.in.EngraverEvent;
import org.unisg.ftengrave.intakeservice.port.in.VacuumGripperEvent;
import org.unisg.ftengrave.intakeservice.port.out.CorrelateMessagePort;
import org.unisg.ftengrave.intakeservice.port.out.ResolveWaitingMessageBusinessKeyPort;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IntakeEventServicesTest {

    @Mock
    private ResolveWaitingMessageBusinessKeyPort resolveWaitingMessageBusinessKeyPort;

    @Mock
    private CorrelateMessagePort correlateMessagePort;

    private ItemArrivedAtIntakeEventService itemArrivedAtIntakeEventService;
    private ItemLeftIntakeEventService itemLeftIntakeEventService;
    private ItemArrivedAtEngraverEventService itemArrivedAtEngraverEventService;

    @BeforeEach
    void setUp() {
        itemArrivedAtIntakeEventService = new ItemArrivedAtIntakeEventService(
                resolveWaitingMessageBusinessKeyPort, correlateMessagePort);
        itemLeftIntakeEventService = new ItemLeftIntakeEventService(
                resolveWaitingMessageBusinessKeyPort, correlateMessagePort);
        itemArrivedAtEngraverEventService = new ItemArrivedAtEngraverEventService(
                resolveWaitingMessageBusinessKeyPort, correlateMessagePort);
    }

    @Test
    void correlatesItemArrivedAtIntakeForWaitingProcess() {
        when(resolveWaitingMessageBusinessKeyPort.resolve(ItemArrivedAtIntakeEventService.ITEM_ARRIVED_AT_INTAKE_MESSAGE))
                .thenReturn("item-42");

        itemArrivedAtIntakeEventService.handle(new VacuumGripperEvent("item-arrived-at-intake"));

        verify(correlateMessagePort).correlateMessage(
                eq(ItemArrivedAtIntakeEventService.ITEM_ARRIVED_AT_INTAKE_MESSAGE),
                eq("item-42"),
                eq(Map.of("itemIdentifier", "item-42", "itemArrivedAtIntake", true)));
    }

    @Test
    void correlatesItemLeftIntakeForWaitingProcess() {
        when(resolveWaitingMessageBusinessKeyPort.resolve(ItemLeftIntakeEventService.ITEM_LEFT_INTAKE_MESSAGE))
                .thenReturn("item-42");

        itemLeftIntakeEventService.handle(new VacuumGripperEvent("item-left-intake"));

        verify(correlateMessagePort).correlateMessage(
                eq(ItemLeftIntakeEventService.ITEM_LEFT_INTAKE_MESSAGE),
                eq("item-42"),
                eq(Map.of("itemIdentifier", "item-42", "itemLeftIntake", true)));
    }

    @Test
    void correlatesItemArrivedAtEngraverForWaitingProcess() {
        when(resolveWaitingMessageBusinessKeyPort.resolve(ItemArrivedAtEngraverEventService.ITEM_ARRIVED_AT_ENGRAVER_MESSAGE))
                .thenReturn("item-42");

        itemArrivedAtEngraverEventService.handle(new EngraverEvent("item-arrived-at-engraver-sink"));

        verify(correlateMessagePort).correlateMessage(
                eq(ItemArrivedAtEngraverEventService.ITEM_ARRIVED_AT_ENGRAVER_MESSAGE),
                eq("item-42"),
                eq(Map.of("itemIdentifier", "item-42", "itemArrivedAtEngraver", true)));
    }

    @Test
    void ignoresUnrelatedEvents() {
        itemArrivedAtIntakeEventService.handle(new VacuumGripperEvent("item-left-output"));
        itemLeftIntakeEventService.handle(new VacuumGripperEvent("item-arrived-at-output"));
        itemArrivedAtEngraverEventService.handle(new EngraverEvent("item-left-engraver-sink"));

        verify(correlateMessagePort, never()).correlateMessage(any(), any(), any());
        verify(resolveWaitingMessageBusinessKeyPort, never()).resolve(any());
    }
}
