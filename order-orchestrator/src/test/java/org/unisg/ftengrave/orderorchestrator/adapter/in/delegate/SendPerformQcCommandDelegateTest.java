package org.unisg.ftengrave.orderorchestrator.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;
import org.unisg.ftengrave.orderorchestrator.port.in.SendPerformQcCommandUseCase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SendPerformQcCommandDelegateTest {

    @Test
    void executeUsesBusinessKeyAndTargetColorToSendPerformQcCommand() throws Exception {
        SendPerformQcCommandUseCase useCase = Mockito.mock(SendPerformQcCommandUseCase.class);
        DelegateExecution execution = Mockito.mock(DelegateExecution.class);
        when(execution.getBusinessKey()).thenReturn("item-42");
        when(execution.getVariable("targetColor")).thenReturn("RED");

        new SendPerformQcCommandDelegate(useCase).execute(execution);

        verify(useCase).sendPerformQcCommand("item-42", ItemColor.RED);
    }
}
