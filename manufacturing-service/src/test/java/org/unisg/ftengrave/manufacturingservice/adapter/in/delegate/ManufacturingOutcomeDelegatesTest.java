package org.unisg.ftengrave.manufacturingservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.unisg.ftengrave.manufacturingservice.port.in.PublishManufacturingCompletedOutcomeUseCase;
import org.unisg.ftengrave.manufacturingservice.port.in.PublishManufacturingFailedOutcomeUseCase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ManufacturingOutcomeDelegatesTest {

    @Test
    void completedDelegatePublishesCompletedOutcomeForBusinessKey() throws Exception {
        PublishManufacturingCompletedOutcomeUseCase useCase =
                Mockito.mock(PublishManufacturingCompletedOutcomeUseCase.class);
        DelegateExecution execution = Mockito.mock(DelegateExecution.class);
        when(execution.getBusinessKey()).thenReturn("item-42");

        new PublishManufacturingCompletedOutcomeDelegate(useCase).execute(execution);

        verify(useCase).publish("item-42");
    }

    @Test
    void failedDelegatePublishesFailedOutcomeForBusinessKey() throws Exception {
        PublishManufacturingFailedOutcomeUseCase useCase =
                Mockito.mock(PublishManufacturingFailedOutcomeUseCase.class);
        DelegateExecution execution = Mockito.mock(DelegateExecution.class);
        when(execution.getBusinessKey()).thenReturn("item-42");

        new PublishManufacturingFailedOutcomeDelegate(useCase).execute(execution);

        verify(useCase).publish("item-42");
    }
}
