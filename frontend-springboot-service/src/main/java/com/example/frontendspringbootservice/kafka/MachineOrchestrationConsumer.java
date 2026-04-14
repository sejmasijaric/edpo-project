package com.example.frontendspringbootservice.kafka;

import com.example.frontendspringbootservice.dto.MachineOrchestrationEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Component;

@Component
public class MachineOrchestrationConsumer {

    private static final Logger log = LoggerFactory.getLogger(MachineOrchestrationConsumer.class);

    private final SimpMessagingTemplate messagingTemplate;

    public MachineOrchestrationConsumer(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @KafkaListener(
            topics = "${app.kafka.topic.machine-orchestration:machine-orchestration}",
            containerFactory = "machineOrchestrationListenerContainerFactory")
    public void consume(MachineOrchestrationEvent event) {
        log.info("Received machine orchestration event: itemIdentifier={}, outcomeType={}",
                event.itemIdentifier(), event.outcomeType());
        messagingTemplate.convertAndSend("/topic/order-updates", event);
    }
}
