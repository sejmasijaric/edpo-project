package org.unisg.ftengrave.manufacturingservice.adapter.out.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaOperations;
import org.unisg.ftengrave.manufacturingservice.adapter.out.kafka.dto.EngraverCommandDto;
import org.unisg.ftengrave.manufacturingservice.adapter.out.kafka.dto.PolishingMachineCommandDto;
import org.unisg.ftengrave.manufacturingservice.adapter.out.kafka.dto.WorkstationTransportCommandDto;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ManufacturingPublisherAdaptersTest {

    @Mock
    private KafkaOperations<String, EngraverCommandDto> engraverKafkaOperations;

    @Mock
    private KafkaOperations<String, WorkstationTransportCommandDto> workstationTransportKafkaOperations;

    @Mock
    private KafkaOperations<String, PolishingMachineCommandDto> polishingMachineKafkaOperations;

    @Test
    void publishSendsRunEngravingCommandToEngraverTopic() {
        EngraverIntegrationProperties properties = new EngraverIntegrationProperties();
        EngraverIntegrationProperties.CommandTypes commandTypes = new EngraverIntegrationProperties.CommandTypes();
        commandTypes.setRunEngraving("run-engraving-command");
        properties.setCommandTypes(commandTypes);
        RunEngravingPublisherAdapter adapter = new RunEngravingPublisherAdapter(
                engraverKafkaOperations, "engraver-commands", properties);

        adapter.publish("item-42");

        verify(engraverKafkaOperations).send(
                eq("engraver-commands"),
                eq("item-42"),
                argThat(command -> command != null && "run-engraving-command".equals(command.getCommandType())));
    }

    @Test
    void publishSendsMoveToPolishingMachineCommandToWorkstationTransportTopic() {
        WorkstationTransportIntegrationProperties properties = new WorkstationTransportIntegrationProperties();
        WorkstationTransportIntegrationProperties.CommandTypes commandTypes =
                new WorkstationTransportIntegrationProperties.CommandTypes();
        commandTypes.setMoveToPolishingMachine("move-item-from-engraver-to-polishing-machine-command");
        properties.setCommandTypes(commandTypes);
        MoveItemFromEngraverToPolishingMachinePublisherAdapter adapter =
                new MoveItemFromEngraverToPolishingMachinePublisherAdapter(
                        workstationTransportKafkaOperations, "workstation-transport-commands", properties);

        adapter.publish("item-42");

        verify(workstationTransportKafkaOperations).send(
                eq("workstation-transport-commands"),
                eq("item-42"),
                argThat(command -> command != null
                        && "move-item-from-engraver-to-polishing-machine-command".equals(command.getCommandType())));
    }

    @Test
    void publishSendsRunPolishingCommandToPolishingMachineTopic() {
        PolishingMachineIntegrationProperties properties = new PolishingMachineIntegrationProperties();
        PolishingMachineIntegrationProperties.CommandTypes commandTypes =
                new PolishingMachineIntegrationProperties.CommandTypes();
        commandTypes.setRunPolishing("run-polishing-command");
        properties.setCommandTypes(commandTypes);
        RunPolishingPublisherAdapter adapter = new RunPolishingPublisherAdapter(
                polishingMachineKafkaOperations, "polishing-machine-commands", properties);

        adapter.publish("item-42");

        verify(polishingMachineKafkaOperations).send(
                eq("polishing-machine-commands"),
                eq("item-42"),
                argThat(command -> command != null && "run-polishing-command".equals(command.getCommandType())));
    }
}
