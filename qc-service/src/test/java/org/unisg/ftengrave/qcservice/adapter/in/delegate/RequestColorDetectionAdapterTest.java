package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.RequestColorDetectionPublisher;

class RequestColorDetectionAdapterTest {

    @Test
    void executePublishesColorDetectionRequestEvent() throws Exception {
        RecordingRequestColorDetectionPublisher publisher = new RecordingRequestColorDetectionPublisher();
        RequestColorDetectionAdapter adapter = new RequestColorDetectionAdapter(publisher);
        DelegateExecution delegateExecution = mock(DelegateExecution.class);

        adapter.execute(delegateExecution);

        assertThat(publisher.publishCalls).isEqualTo(1);
        verify(delegateExecution).setVariable("passedColorCheck", true);
    }

    private static final class RecordingRequestColorDetectionPublisher implements RequestColorDetectionPublisher {

        private int publishCalls;

        @Override
        public void publish() {
            publishCalls++;
        }
    }
}
