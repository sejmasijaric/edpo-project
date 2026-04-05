package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.unisg.ftengrave.qcservice.port.in.PublishQcShippingOutcomeUseCase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublishQcShippingOutcomeDelegateTest {

    @Test
    void executePublishesShippingOutcomeForBusinessKey() throws Exception {
        PublishQcShippingOutcomeUseCase useCase = Mockito.mock(PublishQcShippingOutcomeUseCase.class);
        DelegateExecution execution = Mockito.mock(DelegateExecution.class);
        when(execution.getBusinessKey()).thenReturn("item-42");

        new PublishQcShippingOutcomeDelegate(useCase).execute(execution);

        verify(useCase).publish("item-42");
    }
}
