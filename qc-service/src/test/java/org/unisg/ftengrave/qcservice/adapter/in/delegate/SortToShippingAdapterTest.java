package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import static org.assertj.core.api.Assertions.assertThat;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.qcservice.port.in.SortToShippingUseCase;

class SortToShippingAdapterTest {

    @Test
    void executePublishesSortToShippingEvent() throws Exception {
        RecordingSortToShippingUseCase useCase = new RecordingSortToShippingUseCase();
        SortToShippingAdapter adapter = new SortToShippingAdapter(useCase);

        adapter.execute((DelegateExecution) null);

        assertThat(useCase.sortCalls).isEqualTo(1);
    }

    private static final class RecordingSortToShippingUseCase implements SortToShippingUseCase {

        private int sortCalls;

        @Override
        public void sortToShipping() {
            sortCalls++;
        }
    }
}
