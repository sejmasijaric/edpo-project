package com.example.frontendspringbootservice.service;

import com.example.frontendspringbootservice.dto.LatestItemStatus;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Service
public class LatestItemStatusQueryService {

    public static final String STORE_NAME = "frontend-latest-status-by-item-v1-store";

    private final ConcurrentMap<String, LatestItemStatus> latestStatusByItem = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public LatestItemStatusQueryService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(
            topics = "${app.kafka.topic.latest-item-status:factory.latest-status}",
            containerFactory = "latestItemStatusListenerContainerFactory")
    public void consume(ConsumerRecord<String, LatestItemStatus> record) {
        if (record.key() == null || record.key().isBlank()) {
            return;
        }
        if (record.value() == null) {
            latestStatusByItem.remove(record.key());
            return;
        }
        latestStatusByItem.put(record.key(), record.value());
        messagingTemplate.convertAndSend("/topic/item-status", record.value());
    }

    public Optional<LatestItemStatus> findByItemIdentifier(String itemIdentifier) {
        if (itemIdentifier == null || itemIdentifier.isBlank()) {
            return Optional.empty();
        }
        return Optional.ofNullable(latestStatusByItem.get(itemIdentifier));
    }

    public List<LatestItemStatus> findAll() {
        return List.copyOf(latestStatusByItem.values());
    }
}
