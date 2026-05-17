package com.example.frontendspringbootservice.service;

import com.example.frontendspringbootservice.dto.UserTaskEvent;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class UserTaskService {

    private static final Logger log = LoggerFactory.getLogger(UserTaskService.class);

    private final ObjectMapper objectMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final ConcurrentMap<String, UserTaskEvent> openTasksByItem = new ConcurrentHashMap<>();
    private final List<UserTaskEvent> recentEvents = new ArrayList<>();
    private final int maxRecentEvents = 100;

    public UserTaskService(ObjectMapper objectMapper, SimpMessagingTemplate messagingTemplate) {
        this.objectMapper = objectMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(
            topics = "${app.kafka.topic.user-task-management:user-task-management}",
            containerFactory = "userTaskEventListenerContainerFactory")
    public void consume(ConsumerRecord<String, String> record) {
        UserTaskEvent event = parse(record);
        if (event == null) {
            return;
        }
        log.info("Received user task event: item={}, task={}, category={}, color={}",
                event.itemIdentifier(), event.taskName(), event.taskCategory(), event.targetColor());

        if (event.itemIdentifier() != null) {
            if (isCompleted(event)) {
                openTasksByItem.remove(event.itemIdentifier());
            } else {
                openTasksByItem.put(event.itemIdentifier(), event);
            }
        }

        synchronized (recentEvents) {
            recentEvents.add(0, event);
            if (recentEvents.size() > maxRecentEvents) {
                recentEvents.subList(maxRecentEvents, recentEvents.size()).clear();
            }
        }

        messagingTemplate.convertAndSend("/topic/user-tasks", event);
    }

    public List<UserTaskEvent> openTasks() {
        return openTasksByItem.values().stream()
                .sorted(Comparator.comparing(
                        UserTaskEvent::eventTimestampEpochMillis,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List<UserTaskEvent> recentEvents() {
        synchronized (recentEvents) {
            return List.copyOf(recentEvents);
        }
    }

    private UserTaskEvent parse(ConsumerRecord<String, String> record) {
        String payload = record.value();
        if (payload == null || payload.isBlank()) {
            return null;
        }
        try {
            JsonNode node = objectMapper.readTree(payload);
            String itemIdentifier = textOrNull(node, "itemIdentifier");
            if (itemIdentifier == null) {
                itemIdentifier = record.key();
            }
            String targetColor = textOrNull(node, "targetColor");
            if (targetColor == null) {
                targetColor = textOrNull(node, "itemColor");
            }
            Long ts = node.has("eventTimestampEpochMillis") && node.get("eventTimestampEpochMillis").isNumber()
                    ? node.get("eventTimestampEpochMillis").asLong()
                    : record.timestamp();
            return new UserTaskEvent(
                    itemIdentifier,
                    textOrNull(node, "commandType"),
                    textOrNull(node, "taskName"),
                    textOrNull(node, "taskCategory"),
                    textOrNull(node, "stationName"),
                    targetColor,
                    textOrNull(node, "taskStatus"),
                    textOrNull(node, "errorMessage"),
                    ts);
        } catch (Exception ex) {
            log.warn("Failed to parse user task event: {}", payload, ex);
            return null;
        }
    }

    private boolean isCompleted(UserTaskEvent event) {
        String status = event.taskStatus();
        if (status == null) {
            return false;
        }
        String normalized = status.toLowerCase();
        return normalized.contains("complete") || normalized.contains("resolved") || normalized.contains("closed");
    }

    private String textOrNull(JsonNode node, String field) {
        JsonNode value = node.get(field);
        if (value == null || value.isNull()) {
            return null;
        }
        String text = value.asText();
        return text == null || text.isBlank() ? null : text;
    }
}
