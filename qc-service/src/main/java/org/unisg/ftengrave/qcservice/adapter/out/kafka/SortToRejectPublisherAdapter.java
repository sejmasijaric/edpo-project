package org.unisg.ftengrave.qcservice.adapter.out.kafka;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.dto.SortingMachineEventDto;

@Component
public class SortToRejectPublisherAdapter implements SortToRejectPublisher {

    private static final String SORT_TO_REJECT_EVENT_TYPE = "sort-to-reject";

    private final KafkaOperations<String, SortingMachineEventDto> kafkaOperations;
    private final String sortingMachineTopic;

    public SortToRejectPublisherAdapter(
            KafkaOperations<String, SortingMachineEventDto> kafkaOperations,
            @Value("${kafka.topic.sorting-machine}") String sortingMachineTopic) {
        this.kafkaOperations = kafkaOperations;
        this.sortingMachineTopic = sortingMachineTopic;
    }

    @Override
    public void publish() {
        kafkaOperations.send(sortingMachineTopic, new SortingMachineEventDto(SORT_TO_REJECT_EVENT_TYPE));
    }
}
