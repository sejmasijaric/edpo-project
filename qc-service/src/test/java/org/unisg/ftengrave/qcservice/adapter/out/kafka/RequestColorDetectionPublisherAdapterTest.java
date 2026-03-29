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
class RequestColorDetectionPublisherAdapterTest {

    @Mock
    private KafkaOperations<String, SortingMachineCommandDto> kafkaOperations;

    @Test
    void publishSendsRequestColorDetectionCommandToSortingMachineTopic() {
        SorterIntegrationProperties sorterIntegrationProperties = new SorterIntegrationProperties();
        sorterIntegrationProperties.getCommandTypes().put("color-detection", "request-color-detection");
        RequestColorDetectionPublisherAdapter adapter =
                new RequestColorDetectionPublisherAdapter(
                        kafkaOperations, "sorting-machine", sorterIntegrationProperties);

        adapter.publish();

        verify(kafkaOperations).send(
                eq("sorting-machine"),
                argThat(command -> command != null && "request-color-detection".equals(command.getCommandType())));
    }
}
