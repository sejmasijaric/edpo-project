package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.unisg.ftengrave.qcservice.port.in.PublishQcRejectionOutcomeUseCase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublishQcRejectionOutcomeDelegateTest {

    @Test
    void executePublishesRejectionOutcomeForBusinessKey() throws Exception {
        PublishQcRejectionOutcomeUseCase useCase = Mockito.mock(PublishQcRejectionOutcomeUseCase.class);
        DelegateExecution execution = Mockito.mock(DelegateExecution.class);
        when(execution.getBusinessKey()).thenReturn("item-42");

        new PublishQcRejectionOutcomeDelegate(useCase).execute(execution);

        verify(useCase).publish("item-42");
    }
}
