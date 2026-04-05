package org.unisg.ftengrave.intakeservice.adapter.out.kafka;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.unisg.ftengrave.intakeservice.adapter.out.kafka.dto.InsertItemIntoIntakeCommandDto;
import org.unisg.ftengrave.intakeservice.domain.ItemColor;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserTaskManagementTopicPublisherAdapterTest {

    @Mock
    private KafkaOperations<String, InsertItemIntoIntakeCommandDto> kafkaOperations;

    @Test
    void publishSendsInsertItemIntoIntakeEventToUserTaskManagementTopic() {
        UserTaskManagementTopicPublisherAdapter adapter =
                new UserTaskManagementTopicPublisherAdapter(kafkaOperations, "user-task-management");

        adapter.publish("item-42", ItemColor.BLUE);

        verify(kafkaOperations).send(
                eq("user-task-management"),
                eq("item-42"),
                argThat(event -> event != null
                        && "insert-item-into-intake-command".equals(event.commandType())
                        && "item-intake-station".equals(event.stationName())
                        && ItemColor.BLUE == event.itemColor()));
    }

    @Test
    void publishDefersKafkaSendUntilAfterCommitWhenTransactionSynchronizationIsActive() {
        UserTaskManagementTopicPublisherAdapter adapter =
                new UserTaskManagementTopicPublisherAdapter(kafkaOperations, "user-task-management");

        TransactionSynchronizationManager.initSynchronization();
        try {
            adapter.publish("item-42", ItemColor.WHITE);

            verify(kafkaOperations, never()).send(
                    eq("user-task-management"),
                    eq("item-42"),
                    argThat(event -> event != null));

            for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCommit();
            }

            verify(kafkaOperations).send(
                    eq("user-task-management"),
                    eq("item-42"),
                    argThat(event -> event != null
                            && "insert-item-into-intake-command".equals(event.commandType())
                            && "item-intake-station".equals(event.stationName())
                            && ItemColor.WHITE == event.itemColor()));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
