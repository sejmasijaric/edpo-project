package org.unisg.ftengrave.intakeservice.adapter.out.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.unisg.ftengrave.intakeservice.adapter.out.kafka.dto.VacuumGripperCommandDto;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MoveItemFromInputToEngraverPublisherAdapterTest {

    @Mock
    private KafkaOperations<String, VacuumGripperCommandDto> kafkaOperations;

    @Test
    void publishSendsMoveCommandToVacuumGripperTopic() {
        VacuumGripperIntegrationProperties properties = properties();
        MoveItemFromInputToEngraverPublisherAdapter adapter =
                new MoveItemFromInputToEngraverPublisherAdapter(kafkaOperations, "vacuum-gripper-commands", properties);

        adapter.publish("item-42");

        verify(kafkaOperations).send(
                eq("vacuum-gripper-commands"),
                eq("item-42"),
                argThat(command -> command != null
                        && "move-item-from-input-to-engraver-command".equals(command.getCommandType())));
    }

    @Test
    void publishDefersKafkaSendUntilAfterCommitWhenTransactionSynchronizationIsActive() {
        VacuumGripperIntegrationProperties properties = properties();
        MoveItemFromInputToEngraverPublisherAdapter adapter =
                new MoveItemFromInputToEngraverPublisherAdapter(kafkaOperations, "vacuum-gripper-commands", properties);

        TransactionSynchronizationManager.initSynchronization();
        try {
            adapter.publish("item-42");

            verify(kafkaOperations, never()).send(eq("vacuum-gripper-commands"), eq("item-42"), argThat(command -> command != null));

            for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCommit();
            }

            verify(kafkaOperations).send(
                    eq("vacuum-gripper-commands"),
                    eq("item-42"),
                    argThat(command -> command != null
                            && "move-item-from-input-to-engraver-command".equals(command.getCommandType())));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private VacuumGripperIntegrationProperties properties() {
        VacuumGripperIntegrationProperties properties = new VacuumGripperIntegrationProperties();
        VacuumGripperIntegrationProperties.CommandTypes commandTypes = new VacuumGripperIntegrationProperties.CommandTypes();
        commandTypes.setMoveToEngraver("move-item-from-input-to-engraver-command");
        properties.setCommandTypes(commandTypes);
        return properties;
    }
}
