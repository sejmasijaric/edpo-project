package org.unisg.ftengrave.qcservice.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.dto.SortingMachineCommandDto;
import org.unisg.ftengrave.qcservice.port.out.SortToRejectPort;

@Component
public class SortToRejectPublisherAdapter implements SortToRejectPort {

    private final KafkaOperations<String, SortingMachineCommandDto> kafkaOperations;
    private final String sortingMachineTopic;
    private final SorterIntegrationProperties sorterIntegrationProperties;

    public SortToRejectPublisherAdapter(
            KafkaOperations<String, SortingMachineCommandDto> kafkaOperations,
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
                new SortingMachineCommandDto(
                        sorterIntegrationProperties.getCommandType(SorterSinkNames.REJECTION)));
    }
}
