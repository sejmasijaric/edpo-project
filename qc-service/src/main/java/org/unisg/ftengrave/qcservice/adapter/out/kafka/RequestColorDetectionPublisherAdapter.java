package org.unisg.ftengrave.qcservice.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.dto.SortingMachineCommandDto;
import org.unisg.ftengrave.qcservice.port.out.RequestColorDetectionPort;
import org.unisg.ftengrave.sharedkafka.publisher.TransactionAwareKafkaPublisher;

@Component
public class RequestColorDetectionPublisherAdapter extends TransactionAwareKafkaPublisher<String, SortingMachineCommandDto>
        implements RequestColorDetectionPort {
    private final String sortingMachineTopic;
    private final SorterIntegrationProperties sorterIntegrationProperties;

    public RequestColorDetectionPublisherAdapter(
            KafkaOperations<String, SortingMachineCommandDto> kafkaOperations,
            @Value("${kafka.topic.sorting-machine}") String sortingMachineTopic,
            SorterIntegrationProperties sorterIntegrationProperties) {
        super(kafkaOperations);
        this.sortingMachineTopic = sortingMachineTopic;
        this.sorterIntegrationProperties = sorterIntegrationProperties;
    }

    @Override
    public void publish() {
        publishAfterCommitOrNow(this::sendColorDetectionCommand);
    }

    private void sendColorDetectionCommand() {
        send(
            sortingMachineTopic,
            new SortingMachineCommandDto(
                sorterIntegrationProperties.getCommandType(SorterSinkNames.COLOR_DETECTION)));
    }
}
