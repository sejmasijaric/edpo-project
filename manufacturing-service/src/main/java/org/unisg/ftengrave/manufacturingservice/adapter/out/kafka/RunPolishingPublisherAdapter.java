package org.unisg.ftengrave.manufacturingservice.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.manufacturingservice.adapter.out.kafka.dto.PolishingMachineCommandDto;
import org.unisg.ftengrave.manufacturingservice.port.out.RunPolishingCommandPort;
import org.unisg.ftengrave.sharedkafka.publisher.TransactionAwareKafkaPublisher;

@Component
public class RunPolishingPublisherAdapter extends TransactionAwareKafkaPublisher<String, PolishingMachineCommandDto>
        implements RunPolishingCommandPort {

    private final String polishingMachineTopic;
    private final PolishingMachineIntegrationProperties polishingMachineIntegrationProperties;

    public RunPolishingPublisherAdapter(
            KafkaOperations<String, PolishingMachineCommandDto> kafkaOperations,
            @Value("${kafka.topic.polishing-machine-command}") String polishingMachineTopic,
            PolishingMachineIntegrationProperties polishingMachineIntegrationProperties) {
        super(kafkaOperations);
        this.polishingMachineTopic = polishingMachineTopic;
        this.polishingMachineIntegrationProperties = polishingMachineIntegrationProperties;
    }

    @Override
    public void publish(String itemIdentifier) {
        publishAfterCommitOrNow(() -> send(
                polishingMachineTopic,
                itemIdentifier,
                new PolishingMachineCommandDto(polishingMachineIntegrationProperties.runPolishingCommandType())));
    }
}
