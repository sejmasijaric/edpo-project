package org.unisg.ftengrave.intakeservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.unisg.ftengrave.intakeservice.domain.ItemColor;
import org.unisg.ftengrave.intakeservice.port.in.PublishInsertItemIntoIntakeEventUseCase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PublishInsertItemIntoIntakeEventDelegateTest {

    @Test
    void executePublishesInsertItemIntoIntakeEventForBusinessKeyAndTargetColor() throws Exception {
        PublishInsertItemIntoIntakeEventUseCase useCase = Mockito.mock(PublishInsertItemIntoIntakeEventUseCase.class);
        DelegateExecution execution = Mockito.mock(DelegateExecution.class);
        when(execution.getBusinessKey()).thenReturn("item-42");
        when(execution.getVariable("targetColor")).thenReturn(ItemColor.BLUE);

        new PublishInsertItemIntoIntakeEventDelegate(useCase).execute(execution);

        verify(useCase).publish("item-42", "BLUE");
    }
}
