package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.qcservice.port.in.RequestColorDetectionUseCase;

class SendDetectColorCommandDelegateTest {

    @Test
    void executePublishesColorDetectionRequestEvent() throws Exception {
        RecordingRequestColorDetectionUseCase useCase = new RecordingRequestColorDetectionUseCase();
        SendDetectColorCommandDelegate delegate = new SendDetectColorCommandDelegate(useCase);

        delegate.execute(null);

        assertThat(useCase.requestCalls).isEqualTo(1);
    }

    private static final class RecordingRequestColorDetectionUseCase implements RequestColorDetectionUseCase {

        private int requestCalls;

        @Override
        public void requestColorDetection() {
            requestCalls++;
        }
    }
}
