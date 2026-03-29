package org.unisg.ftengrave.qcservice.adapter.out.kafka;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaOperations;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.dto.SortingMachineCommandDto;

@ExtendWith(MockitoExtension.class)
class SortToShippingPublisherAdapterTest {

    @Mock
    private KafkaOperations<String, SortingMachineCommandDto> kafkaOperations;

    @Test
    void publishSendsSortToShippingCommandToSortingMachineTopic() {
        SorterIntegrationProperties sorterIntegrationProperties = new SorterIntegrationProperties();
        sorterIntegrationProperties.getCommandTypes().put("shipping", "request-sort-to-shipping");
        SortToShippingPublisherAdapter adapter =
                new SortToShippingPublisherAdapter(kafkaOperations, "sorting-machine", sorterIntegrationProperties);

        adapter.publish();

        verify(kafkaOperations).send(
                eq("sorting-machine"),
                argThat(command -> command != null && "request-sort-to-shipping".equals(command.getCommandType())));
    }
}
