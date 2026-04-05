package org.unisg.ftengrave.intakeservice.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.intakeservice.adapter.out.kafka.dto.InsertItemIntoIntakeEventDto;
import org.unisg.ftengrave.intakeservice.domain.ItemColor;
import org.unisg.ftengrave.intakeservice.port.out.PublishInsertItemIntoIntakeEventPort;
import org.unisg.ftengrave.sharedkafka.publisher.TransactionAwareKafkaPublisher;

@Component
public class UserTaskManagementTopicPublisherAdapter
        extends TransactionAwareKafkaPublisher<String, InsertItemIntoIntakeEventDto>
        implements PublishInsertItemIntoIntakeEventPort {

    static final String INSERT_ITEM_INTO_INTAKE_EVENT = "insert-item-into-intake-event";
    static final String ITEM_INTAKE_STATION = "item-intake-station";

    private final String userTaskManagementTopic;

    public UserTaskManagementTopicPublisherAdapter(
            KafkaOperations<String, InsertItemIntoIntakeEventDto> kafkaOperations,
            @Value("${kafka.topic.user-task-management}") String userTaskManagementTopic) {
        super(kafkaOperations);
        this.userTaskManagementTopic = userTaskManagementTopic;
    }

    @Override
    public void publish(String itemIdentifier, ItemColor itemColor) {
        publishAfterCommitOrNow(() -> send(
                userTaskManagementTopic,
                itemIdentifier,
                new InsertItemIntoIntakeEventDto(
                        INSERT_ITEM_INTO_INTAKE_EVENT,
                        ITEM_INTAKE_STATION,
                        itemColor)));
    }
}
