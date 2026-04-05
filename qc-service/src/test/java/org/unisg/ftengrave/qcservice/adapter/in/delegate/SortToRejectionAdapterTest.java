package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.qcservice.port.in.SortToRejectUseCase;

class SortToRejectionAdapterTest {

    @Test
    void executePublishesSortToRejectEvent() throws Exception {
        RecordingSortToRejectUseCase useCase = new RecordingSortToRejectUseCase();
        SortToRejectionAdapter adapter = new SortToRejectionAdapter(useCase);

        adapter.execute((DelegateExecution) null);

        assertThat(useCase.sortCalls).isEqualTo(1);
    }

    private static final class RecordingSortToRejectUseCase implements SortToRejectUseCase {

        private int sortCalls;

        @Override
        public void sortToReject() {
            sortCalls++;
        }
    }
}
