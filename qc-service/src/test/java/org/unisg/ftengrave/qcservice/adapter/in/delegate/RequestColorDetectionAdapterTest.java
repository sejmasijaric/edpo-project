package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.RequestColorDetectionPublisher;

class RequestColorDetectionAdapterTest {

    @Test
    void executePublishesColorDetectionRequestEvent() throws Exception {
        RecordingRequestColorDetectionPublisher publisher = new RecordingRequestColorDetectionPublisher();
        RequestColorDetectionAdapter adapter = new RequestColorDetectionAdapter(publisher);

        adapter.execute(null);

        assertThat(publisher.publishCalls).isEqualTo(1);
    }

    private static final class RecordingRequestColorDetectionPublisher implements RequestColorDetectionPublisher {

        private int publishCalls;

        @Override
        public void publish() {
            publishCalls++;
        }
    }
}
