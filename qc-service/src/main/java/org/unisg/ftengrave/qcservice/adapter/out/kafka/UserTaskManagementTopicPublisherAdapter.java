package org.unisg.ftengrave.qcservice.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.dto.UserTaskIssuedEventDto;
import org.unisg.ftengrave.qcservice.port.out.PublishUserTaskIssuedEventPort;
import org.unisg.ftengrave.sharedkafka.publisher.TransactionAwareKafkaPublisher;

@Component
public class UserTaskManagementTopicPublisherAdapter
        extends TransactionAwareKafkaPublisher<String, UserTaskIssuedEventDto>
        implements PublishUserTaskIssuedEventPort {

    private final String userTaskManagementTopic;

    public UserTaskManagementTopicPublisherAdapter(
            KafkaOperations<String, UserTaskIssuedEventDto> kafkaOperations,
            @Value("${kafka.topic.user-task-management}") String userTaskManagementTopic) {
        super(kafkaOperations);
        this.userTaskManagementTopic = userTaskManagementTopic;
    }

    @Override
    public void publish(String itemIdentifier, String commandType, String taskName, String taskCategory,
            String stationName, String targetColor) {
        publishAfterCommitOrNow(() -> send(
                userTaskManagementTopic,
                itemIdentifier,
                new UserTaskIssuedEventDto(commandType, taskName, taskCategory, stationName, targetColor)));
    }
}
