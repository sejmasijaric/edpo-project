package org.unisg.ftengrave.manufacturingservice.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.manufacturingservice.adapter.out.kafka.dto.WorkstationTransportCommandDto;
import org.unisg.ftengrave.manufacturingservice.port.out.MoveItemFromEngraverToPolishingMachineCommandPort;
import org.unisg.ftengrave.sharedkafka.publisher.TransactionAwareKafkaPublisher;

@Component
public class MoveItemFromEngraverToPolishingMachinePublisherAdapter
        extends TransactionAwareKafkaPublisher<String, WorkstationTransportCommandDto>
        implements MoveItemFromEngraverToPolishingMachineCommandPort {

    private final String workstationTransportTopic;
    private final WorkstationTransportIntegrationProperties workstationTransportIntegrationProperties;

    public MoveItemFromEngraverToPolishingMachinePublisherAdapter(
            KafkaOperations<String, WorkstationTransportCommandDto> kafkaOperations,
            @Value("${kafka.topic.workstation-transport}") String workstationTransportTopic,
            WorkstationTransportIntegrationProperties workstationTransportIntegrationProperties) {
        super(kafkaOperations);
        this.workstationTransportTopic = workstationTransportTopic;
        this.workstationTransportIntegrationProperties = workstationTransportIntegrationProperties;
    }

    @Override
    public void publish(String itemIdentifier) {
        publishAfterCommitOrNow(() -> send(
                workstationTransportTopic,
                itemIdentifier,
                new WorkstationTransportCommandDto(workstationTransportIntegrationProperties.moveToPolishingMachineCommandType())));
    }
}
