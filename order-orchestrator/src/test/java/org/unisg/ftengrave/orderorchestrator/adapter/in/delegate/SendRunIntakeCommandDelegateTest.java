package org.unisg.ftengrave.orderorchestrator.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;
import org.unisg.ftengrave.orderorchestrator.port.in.SendRunIntakeCommandUseCase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SendRunIntakeCommandDelegateTest {

    @Test
    void executeUsesBusinessKeyAndTargetColorToSendRunIntakeCommand() throws Exception {
        SendRunIntakeCommandUseCase useCase = Mockito.mock(SendRunIntakeCommandUseCase.class);
        DelegateExecution execution = Mockito.mock(DelegateExecution.class);
        when(execution.getBusinessKey()).thenReturn("item-42");
        when(execution.getVariable("targetColor")).thenReturn("RED");

        new SendRunIntakeCommandDelegate(useCase).execute(execution);

        verify(useCase).sendRunIntakeCommand("item-42", ItemColor.RED);
    }
}
