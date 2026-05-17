package com.example.frontendspringbootservice.service;

import com.example.frontendspringbootservice.dto.UserTaskEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class UserTaskServiceTest {

    private SimpMessagingTemplate messagingTemplate;
    private UserTaskService service;

    @BeforeEach
    void setUp() {
        messagingTemplate = mock(SimpMessagingTemplate.class);
        service = new UserTaskService(JsonMapper.builder().build(), messagingTemplate);
    }

    @Test
    void consume_publishesEventOverWebSocket() {
        var record = record("item-1", """
                {"commandType":"insert-item-into-intake-command","taskName":"Insert Item",
                 "taskCategory":"normal","stationName":"item-intake-station","itemColor":"RED"}
                """.replace("\n", ""));

        service.consume(record);

        ArgumentCaptor<UserTaskEvent> captor = ArgumentCaptor.forClass(UserTaskEvent.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/user-tasks"), captor.capture());

        UserTaskEvent event = captor.getValue();
        assertThat(event.itemIdentifier()).isEqualTo("item-1");
        assertThat(event.commandType()).isEqualTo("insert-item-into-intake-command");
        assertThat(event.taskName()).isEqualTo("Insert Item");
        assertThat(event.targetColor()).isEqualTo("RED");
        assertThat(event.stationName()).isEqualTo("item-intake-station");
    }

    @Test
    void consume_prefersTargetColorOverItemColor() {
        var record = record("item-2", """
                {"commandType":"check-quality-user-task-issued","taskName":"Check Quality",
                 "stationName":"qc","targetColor":"BLUE"}
                """);

        service.consume(record);

        assertThat(service.openTasks()).hasSize(1);
        assertThat(service.openTasks().get(0).targetColor()).isEqualTo("BLUE");
    }

    @Test
    void openTasks_returnsLatestPerItemAndCommand() {
        service.consume(record("item-3", """
                {"commandType":"insert-item-into-intake-command","taskName":"Insert Item",
                 "stationName":"intake","itemColor":"WHITE"}
                """));
        assertThat(service.openTasks()).hasSize(1);
    }

    @Test
    void openTasks_removesCompletedTasks() {
        service.consume(record("item-4", """
                {"commandType":"check-quality-user-task-issued","taskName":"Check Quality",
                 "stationName":"qc"}
                """));
        assertThat(service.openTasks()).hasSize(1);

        service.consume(record("item-4", """
                {"commandType":"check-quality-user-task-issued","taskName":"Check Quality",
                 "stationName":"qc","taskStatus":"completed"}
                """));
        assertThat(service.openTasks()).isEmpty();
    }

    @Test
    void recentEvents_retainsHistoryInDescendingOrder() {
        service.consume(record("item-5", "{\"taskName\":\"Insert Item\",\"commandType\":\"a\"}"));
        service.consume(record("item-5", "{\"taskName\":\"Check Quality\",\"commandType\":\"b\"}"));

        List<UserTaskEvent> recent = service.recentEvents();
        assertThat(recent).hasSize(2);
        assertThat(recent.get(0).commandType()).isEqualTo("b");
        assertThat(recent.get(1).commandType()).isEqualTo("a");
    }

    @Test
    void consume_skipsBlankPayload() {
        service.consume(record("item-6", "  "));
        assertThat(service.openTasks()).isEmpty();
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void consume_skipsInvalidJson() {
        service.consume(record("item-7", "not-json"));
        assertThat(service.openTasks()).isEmpty();
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void consume_includesErrorMessageFromTopic() {
        service.consume(record("item-8", """
                {"commandType":"resolve-issue","taskName":"Resolve Issue",
                 "taskCategory":"error","errorMessage":"sensor offline"}
                """));

        UserTaskEvent event = service.openTasks().get(0);
        assertThat(event.errorMessage()).isEqualTo("sensor offline");
        assertThat(event.isError()).isTrue();
    }

    private ConsumerRecord<String, String> record(String key, String payload) {
        return new ConsumerRecord<>("user-task-management", 0, 0L, key, payload);
    }
}
