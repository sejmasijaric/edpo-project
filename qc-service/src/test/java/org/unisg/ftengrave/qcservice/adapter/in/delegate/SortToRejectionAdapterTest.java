package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.SortToRejectPublisher;

class SortToRejectionAdapterTest {

    @Test
    void executePublishesSortToRejectEvent() throws Exception {
        RecordingSortToRejectPublisher publisher = new RecordingSortToRejectPublisher();
        SortToRejectionAdapter adapter = new SortToRejectionAdapter(publisher);

        adapter.execute((DelegateExecution) null);

        assertThat(publisher.publishCalls).isEqualTo(1);
    }

    private static final class RecordingSortToRejectPublisher implements SortToRejectPublisher {

        private int publishCalls;

        @Override
        public void publish() {
            publishCalls++;
        }
    }
}
