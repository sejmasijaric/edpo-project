package org.unisg.ftengrave.qcservice.adapter.out.kafka;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaOperations;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.dto.SortingMachineCommandDto;

@ExtendWith(MockitoExtension.class)
class SortToRetryPublisherAdapterTest {

    @Mock
    private KafkaOperations<String, SortingMachineCommandDto> kafkaOperations;

    @Test
    void publishSendsSortToRetryCommandToSortingMachineTopic() {
        SorterIntegrationProperties sorterIntegrationProperties = new SorterIntegrationProperties();
        sorterIntegrationProperties.getCommandTypes().put("retry", "request-sort-to-retry");
        SortToRetryPublisherAdapter adapter =
                new SortToRetryPublisherAdapter(kafkaOperations, "sorting-machine-commands", sorterIntegrationProperties);

        adapter.publish();

        verify(kafkaOperations).send(
                eq("sorting-machine-commands"),
                argThat(command -> command != null && "request-sort-to-retry".equals(command.getCommandType())));
    }
}
