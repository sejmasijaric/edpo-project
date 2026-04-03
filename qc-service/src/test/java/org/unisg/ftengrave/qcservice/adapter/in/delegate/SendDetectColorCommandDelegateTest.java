package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.RequestColorDetectionPublisher;

class SendDetectColorCommandDelegateTest {

    @Test
    void executePublishesColorDetectionRequestEvent() throws Exception {
        RecordingRequestColorDetectionPublisher publisher = new RecordingRequestColorDetectionPublisher();
        SendDetectColorCommandDelegate delegate = new SendDetectColorCommandDelegate(publisher);

        delegate.execute(null);

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
