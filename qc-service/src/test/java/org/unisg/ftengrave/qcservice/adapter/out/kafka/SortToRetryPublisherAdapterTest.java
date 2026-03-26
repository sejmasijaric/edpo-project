package org.unisg.ftengrave.qcservice.adapter.out.kafka;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaOperations;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.dto.SortingMachineEventDto;

@ExtendWith(MockitoExtension.class)
class SortToRetryPublisherAdapterTest {

    @Mock
    private KafkaOperations<String, SortingMachineEventDto> kafkaOperations;

    @Test
    void publishSendsSortToRetryEventToSortingMachineTopic() {
        SortToRetryPublisherAdapter adapter =
                new SortToRetryPublisherAdapter(kafkaOperations, "sorting-machine");

        adapter.publish();

        verify(kafkaOperations).send(
                eq("sorting-machine"),
                argThat(event -> event != null && "sort-to-retry".equals(event.getEventType())));
    }
}
