package org.unisg.ftengrave.intakeservice.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.intakeservice.adapter.out.kafka.dto.InsertItemIntoIntakeCommandDto;
import org.unisg.ftengrave.intakeservice.domain.ItemColor;
import org.unisg.ftengrave.intakeservice.port.out.PublishInsertItemIntoIntakeCommandPort;
import org.unisg.ftengrave.sharedkafka.publisher.TransactionAwareKafkaPublisher;

@Component
public class UserTaskManagementTopicPublisherAdapter
        extends TransactionAwareKafkaPublisher<String, InsertItemIntoIntakeCommandDto>
        implements PublishInsertItemIntoIntakeCommandPort {

    static final String INSERT_ITEM_INTO_INTAKE_EVENT = "insert-item-into-intake-command";
    static final String ITEM_INTAKE_STATION = "item-intake-station";

    private final String userTaskManagementTopic;

    public UserTaskManagementTopicPublisherAdapter(
            KafkaOperations<String, InsertItemIntoIntakeCommandDto> kafkaOperations,
            @Value("${kafka.topic.user-task-management}") String userTaskManagementTopic) {
        super(kafkaOperations);
        this.userTaskManagementTopic = userTaskManagementTopic;
    }

    @Override
    public void publish(String itemIdentifier, ItemColor itemColor) {
        publishAfterCommitOrNow(() -> send(
                userTaskManagementTopic,
                itemIdentifier,
                new InsertItemIntoIntakeCommandDto(
                        INSERT_ITEM_INTO_INTAKE_EVENT,
                        ITEM_INTAKE_STATION,
                        itemColor)));
    }
}
