package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.SortToRetryPublisher;

class SortToRetryAdapterTest {

    @Test
    void executePublishesSortToRetryEvent() throws Exception {
        RecordingSortToRetryPublisher publisher = new RecordingSortToRetryPublisher();
        SortToRetryAdapter adapter = new SortToRetryAdapter(publisher);

        adapter.execute((DelegateExecution) null);

        assertThat(publisher.publishCalls).isEqualTo(1);
    }

    private static final class RecordingSortToRetryPublisher implements SortToRetryPublisher {

        private int publishCalls;

        @Override
        public void publish() {
            publishCalls++;
        }
    }
}
