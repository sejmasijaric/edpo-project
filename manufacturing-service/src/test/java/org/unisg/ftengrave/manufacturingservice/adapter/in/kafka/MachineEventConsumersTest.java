package org.unisg.ftengrave.manufacturingservice.adapter.in.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.unisg.ftengrave.manufacturingservice.adapter.in.kafka.dto.EngraverEventDto;
import org.unisg.ftengrave.manufacturingservice.adapter.in.kafka.dto.PolishingMachineEventDto;
import org.unisg.ftengrave.manufacturingservice.adapter.in.kafka.dto.WorkstationTransportEventDto;
import org.unisg.ftengrave.manufacturingservice.port.in.EngraverEvent;
import org.unisg.ftengrave.manufacturingservice.port.in.HandleItemArrivedAtEngraverSinkEventUseCase;
import org.unisg.ftengrave.manufacturingservice.port.in.HandleItemArrivedAtPolishingMachineOutputEventUseCase;
import org.unisg.ftengrave.manufacturingservice.port.in.HandleItemLeftEngraverSinkEventUseCase;
import org.unisg.ftengrave.manufacturingservice.port.in.HandleWtMoveCompletedEventUseCase;
import org.unisg.ftengrave.manufacturingservice.port.in.PolishingMachineEvent;
import org.unisg.ftengrave.manufacturingservice.port.in.WorkstationTransportEvent;

import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MachineEventConsumersTest {

    @Mock
    private HandleItemLeftEngraverSinkEventUseCase handleItemLeftEngraverSinkEventUseCase;

    @Mock
    private HandleItemArrivedAtEngraverSinkEventUseCase handleItemArrivedAtEngraverSinkEventUseCase;

    @Mock
    private HandleWtMoveCompletedEventUseCase handleWtMoveCompletedEventUseCase;

    @Mock
    private HandleItemArrivedAtPolishingMachineOutputEventUseCase handleItemArrivedAtPolishingMachineOutputEventUseCase;

    @InjectMocks
    private EngraverEventConsumer engraverEventConsumer;

    @InjectMocks
    private WorkstationTransportEventConsumer workstationTransportEventConsumer;

    @InjectMocks
    private PolishingMachineEventConsumer polishingMachineEventConsumer;

    @Test
    void delegatesEngraverEventsToApplicationServices() {
        engraverEventConsumer.consume(new EngraverEventDto("item-left-engraver-sink"));

        verify(handleItemLeftEngraverSinkEventUseCase).handle(new EngraverEvent("item-left-engraver-sink"));
        verify(handleItemArrivedAtEngraverSinkEventUseCase).handle(new EngraverEvent("item-left-engraver-sink"));
    }

    @Test
    void delegatesWorkstationTransportEventsToApplicationService() {
        workstationTransportEventConsumer.consume(new WorkstationTransportEventDto("wt-move-completed"));

        verify(handleWtMoveCompletedEventUseCase).handle(new WorkstationTransportEvent("wt-move-completed"));
    }

    @Test
    void delegatesPolishingMachineEventsToApplicationService() {
        polishingMachineEventConsumer.consume(new PolishingMachineEventDto("item-arrived-at-polishing-machine-output"));

        verify(handleItemArrivedAtPolishingMachineOutputEventUseCase).handle(
                new PolishingMachineEvent("item-arrived-at-polishing-machine-output"));
    }
}
