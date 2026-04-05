package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.qcservice.port.in.SortToRetryUseCase;

class SortToRetryAdapterTest {

    @Test
    void executePublishesSortToRetryEvent() throws Exception {
        RecordingSortToRetryUseCase useCase = new RecordingSortToRetryUseCase();
        SortToRetryAdapter adapter = new SortToRetryAdapter(useCase);

        adapter.execute((DelegateExecution) null);

        assertThat(useCase.sortCalls).isEqualTo(1);
    }

    private static final class RecordingSortToRetryUseCase implements SortToRetryUseCase {

        private int sortCalls;

        @Override
        public void sortToRetry() {
            sortCalls++;
        }
    }
}
