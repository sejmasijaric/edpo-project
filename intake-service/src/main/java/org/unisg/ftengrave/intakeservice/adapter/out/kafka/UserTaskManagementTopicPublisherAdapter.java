package org.unisg.ftengrave.intakeservice.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.intakeservice.adapter.out.kafka.dto.InsertItemIntoIntakeCommandDto;
import org.unisg.ftengrave.intakeservice.domain.ItemColor;
import org.unisg.ftengrave.intakeservice.port.out.PublishInsertItemIntoIntakeCommandPort;
import org.unisg.ftengrave.intakeservice.port.out.PublishUserTaskIssuedEventPort;
import org.unisg.ftengrave.sharedkafka.publisher.TransactionAwareKafkaPublisher;

@Component
public class UserTaskManagementTopicPublisherAdapter
        extends TransactionAwareKafkaPublisher<String, InsertItemIntoIntakeCommandDto>
        implements PublishInsertItemIntoIntakeCommandPort, PublishUserTaskIssuedEventPort {

    static final String INSERT_ITEM_INTO_INTAKE_EVENT = "insert-item-into-intake-command";
    static final String INSERT_ITEM_INTO_INTAKE_TASK = "Insert Item";
    static final String NORMAL_TASK = "normal";
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
                        INSERT_ITEM_INTO_INTAKE_TASK,
                        NORMAL_TASK,
                        ITEM_INTAKE_STATION,
                        itemColor)));
    }

    @Override
    public void publish(String itemIdentifier, String commandType, String taskName, String taskCategory,
            String stationName, String targetColor) {
        publishAfterCommitOrNow(() -> send(
                userTaskManagementTopic,
                itemIdentifier,
                new InsertItemIntoIntakeCommandDto(
                        commandType,
                        taskName,
                        taskCategory,
                        stationName,
                        targetColor == null ? null : ItemColor.valueOf(targetColor))));
    }
}
