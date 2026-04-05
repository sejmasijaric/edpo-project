package org.unisg.ftengrave.qcservice.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.dto.SortingMachineCommandDto;
import org.unisg.ftengrave.qcservice.port.out.SortToRetryPort;
import org.unisg.ftengrave.sharedkafka.publisher.TransactionAwareKafkaPublisher;

@Component
public class SortToRetryPublisherAdapter extends TransactionAwareKafkaPublisher<String, SortingMachineCommandDto>
        implements SortToRetryPort {
    private final String sortingMachineTopic;
    private final SorterIntegrationProperties sorterIntegrationProperties;

    public SortToRetryPublisherAdapter(
            KafkaOperations<String, SortingMachineCommandDto> kafkaOperations,
            @Value("${kafka.topic.sorting-machine}") String sortingMachineTopic,
            SorterIntegrationProperties sorterIntegrationProperties) {
        super(kafkaOperations);
        this.sortingMachineTopic = sortingMachineTopic;
        this.sorterIntegrationProperties = sorterIntegrationProperties;
    }

    @Override
    public void publish() {
        publishAfterCommitOrNow(() -> send(
                sortingMachineTopic,
                new SortingMachineCommandDto(sorterIntegrationProperties.getCommandType(SorterSinkNames.RETRY))));
    }
}
