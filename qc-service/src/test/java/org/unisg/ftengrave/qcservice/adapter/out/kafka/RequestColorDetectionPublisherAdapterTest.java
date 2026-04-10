package org.unisg.ftengrave.qcservice.adapter.out.kafka;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.dto.SortingMachineCommandDto;

@ExtendWith(MockitoExtension.class)
class RequestColorDetectionPublisherAdapterTest {

    @Mock
    private KafkaOperations<String, SortingMachineCommandDto> kafkaOperations;

    @Test
    void publishSendsRequestColorDetectionCommandToSortingMachineTopic() {
        SorterIntegrationProperties sorterIntegrationProperties = new SorterIntegrationProperties();
        sorterIntegrationProperties.getCommandTypes().put("color-detection", "request-color-detection");
        RequestColorDetectionPublisherAdapter adapter =
                new RequestColorDetectionPublisherAdapter(
                        kafkaOperations, "sorting-machine-commands", sorterIntegrationProperties);

        adapter.publish();

        verify(kafkaOperations).send(
                eq("sorting-machine-commands"),
                argThat(command -> command != null && "request-color-detection".equals(command.getCommandType())));
    }

    @Test
    void publishDefersKafkaSendUntilAfterCommitWhenTransactionSynchronizationIsActive() {
        SorterIntegrationProperties sorterIntegrationProperties = new SorterIntegrationProperties();
        sorterIntegrationProperties.getCommandTypes().put("color-detection", "request-color-detection");
        RequestColorDetectionPublisherAdapter adapter =
                new RequestColorDetectionPublisherAdapter(
                        kafkaOperations, "sorting-machine-commands", sorterIntegrationProperties);

        TransactionSynchronizationManager.initSynchronization();
        try {
            adapter.publish();

            verify(kafkaOperations, never()).send(eq("sorting-machine-commands"), argThat(command -> command != null));

            for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
                synchronization.afterCommit();
            }

            verify(kafkaOperations).send(
                    eq("sorting-machine-commands"),
                    argThat(command -> command != null && "request-color-detection".equals(command.getCommandType())));
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
