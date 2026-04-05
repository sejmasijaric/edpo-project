package org.unisg.ftengrave.intakeservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.unisg.ftengrave.intakeservice.port.in.PublishIntakeCompletedOutcomeUseCase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublishIntakeCompletedOutcomeDelegateTest {

    @Test
    void executePublishesIntakeCompletedOutcomeForBusinessKey() throws Exception {
        PublishIntakeCompletedOutcomeUseCase useCase = Mockito.mock(PublishIntakeCompletedOutcomeUseCase.class);
        DelegateExecution execution = Mockito.mock(DelegateExecution.class);
        when(execution.getBusinessKey()).thenReturn("item-42");

        new PublishIntakeCompletedOutcomeDelegate(useCase).execute(execution);

        verify(useCase).publish("item-42");
    }
}
