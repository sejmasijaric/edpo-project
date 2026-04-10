package org.unisg.ftengrave.manufacturingservice.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.manufacturingservice.adapter.out.kafka.dto.EngraverCommandDto;
import org.unisg.ftengrave.manufacturingservice.port.out.RunEngravingCommandPort;
import org.unisg.ftengrave.sharedkafka.publisher.TransactionAwareKafkaPublisher;

@Component
public class RunEngravingPublisherAdapter extends TransactionAwareKafkaPublisher<String, EngraverCommandDto>
        implements RunEngravingCommandPort {

    private final String engraverTopic;
    private final EngraverIntegrationProperties engraverIntegrationProperties;

    public RunEngravingPublisherAdapter(
            KafkaOperations<String, EngraverCommandDto> kafkaOperations,
            @Value("${kafka.topic.engraver-command}") String engraverTopic,
            EngraverIntegrationProperties engraverIntegrationProperties) {
        super(kafkaOperations);
        this.engraverTopic = engraverTopic;
        this.engraverIntegrationProperties = engraverIntegrationProperties;
    }

    @Override
    public void publish(String itemIdentifier) {
        publishAfterCommitOrNow(() -> send(
                engraverTopic,
                itemIdentifier,
                new EngraverCommandDto(engraverIntegrationProperties.runEngravingCommandType())));
    }
}
