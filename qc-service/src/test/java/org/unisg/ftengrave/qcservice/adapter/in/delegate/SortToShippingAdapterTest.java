package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.SortToShippingPublisher;

class SortToShippingAdapterTest {

    @Test
    void executePublishesSortToShippingEvent() throws Exception {
        RecordingSortToShippingPublisher publisher = new RecordingSortToShippingPublisher();
        SortToShippingAdapter adapter = new SortToShippingAdapter(publisher);

        adapter.execute((DelegateExecution) null);

        assertThat(publisher.publishCalls).isEqualTo(1);
    }

    private static final class RecordingSortToShippingPublisher implements SortToShippingPublisher {

        private int publishCalls;

        @Override
        public void publish() {
            publishCalls++;
        }
    }
}
