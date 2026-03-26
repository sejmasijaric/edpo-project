package org.unisg.ftengrave.qcservice.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.dto.SortingMachineEventDto;

@Component
public class SortToRetryPublisherAdapter implements SortToRetryPublisher {

    private final KafkaOperations<String, SortingMachineEventDto> kafkaOperations;
    private final String sortingMachineTopic;
    private final SorterIntegrationProperties sorterIntegrationProperties;

    public SortToRetryPublisherAdapter(
            KafkaOperations<String, SortingMachineEventDto> kafkaOperations,
            @Value("${kafka.topic.sorting-machine}") String sortingMachineTopic,
            SorterIntegrationProperties sorterIntegrationProperties) {
        this.kafkaOperations = kafkaOperations;
        this.sortingMachineTopic = sortingMachineTopic;
        this.sorterIntegrationProperties = sorterIntegrationProperties;
    }

    @Override
    public void publish() {
        kafkaOperations.send(
                sortingMachineTopic,
                new SortingMachineEventDto(sorterIntegrationProperties.getEventType(SorterSinkNames.RETRY)));
    }
}
