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
import org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.dto.MessageProcessDto;

@ExtendWith(MockitoExtension.class)
class ItemArrivedAtQcEventServiceTest {

    @Mock
    private WaitingMessageBusinessKeyResolver waitingMessageBusinessKeyResolver;

    @Mock
    private MessageCorrelationService messageCorrelationService;

    private ItemArrivedAtQcEventService service;

    @BeforeEach
    void setUp() {
        service = new ItemArrivedAtQcEventService(waitingMessageBusinessKeyResolver, messageCorrelationService);
    }

    @Test
    void correlatesItemArrivedAtQcForWaitingProcess() {
        when(waitingMessageBusinessKeyResolver.resolve(ItemArrivedAtQcEventService.ITEM_ARRIVED_AT_QC_MESSAGE))
                .thenReturn("item-42");

        service.handle(new SortingMachineEventDto("item-arrived-at-qc"));

        CamundaMessageDto expectedMessage = CamundaMessageDto.builder()
                .dto(MessageProcessDto.builder()
                        .itemIdentifier("item-42")
                        .build())
                .build();

        verify(messageCorrelationService).correlateMessage(
                eq(expectedMessage),
                eq(ItemArrivedAtQcEventService.ITEM_ARRIVED_AT_QC_MESSAGE));
    }

    @Test
    void ignoresUnrelatedSorterEvents() {
        service.handle(new SortingMachineEventDto("color-detected", "blue"));

        verify(messageCorrelationService, never()).correlateMessage(any(), any());
        verify(waitingMessageBusinessKeyResolver, never()).resolve(any());
    }

    @Test
    void ignoresItemArrivedAtQcWhenNoProcessWaitsForMessage() {
        when(waitingMessageBusinessKeyResolver.resolve(ItemArrivedAtQcEventService.ITEM_ARRIVED_AT_QC_MESSAGE))
                .thenReturn(null);

        service.handle(new SortingMachineEventDto("item-arrived-at-qc"));

        verify(messageCorrelationService, never()).correlateMessage(any(), any());
    }
}
