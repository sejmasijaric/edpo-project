package org.unisg.ftengrave.intakeservice.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.intakeservice.adapter.out.kafka.dto.VacuumGripperCommandDto;
import org.unisg.ftengrave.intakeservice.port.out.MoveItemFromInputToEngraverPort;
import org.unisg.ftengrave.sharedkafka.publisher.TransactionAwareKafkaPublisher;

@Component
public class MoveItemFromInputToEngraverPublisherAdapter
        extends TransactionAwareKafkaPublisher<String, VacuumGripperCommandDto>
        implements MoveItemFromInputToEngraverPort {

    private final String vacuumGripperTopic;
    private final VacuumGripperIntegrationProperties vacuumGripperIntegrationProperties;

    public MoveItemFromInputToEngraverPublisherAdapter(
            KafkaOperations<String, VacuumGripperCommandDto> kafkaOperations,
            @Value("${kafka.topic.vacuum-gripper-command}") String vacuumGripperTopic,
            VacuumGripperIntegrationProperties vacuumGripperIntegrationProperties) {
        super(kafkaOperations);
        this.vacuumGripperTopic = vacuumGripperTopic;
        this.vacuumGripperIntegrationProperties = vacuumGripperIntegrationProperties;
    }

    @Override
    public void publish(String itemIdentifier) {
        publishAfterCommitOrNow(() -> send(
                vacuumGripperTopic,
                itemIdentifier,
                new VacuumGripperCommandDto(vacuumGripperIntegrationProperties.moveToEngraverCommandType())));
    }
}
