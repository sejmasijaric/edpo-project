package org.unisg.ftengrave.orderorchestrator.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.unisg.ftengrave.orderorchestrator.port.in.SendRunProductionCommandUseCase;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SendRunProductionCommandDelegateTest {

    @Test
    void executeUsesBusinessKeyToSendRunProductionCommand() throws Exception {
        SendRunProductionCommandUseCase useCase = Mockito.mock(SendRunProductionCommandUseCase.class);
        DelegateExecution execution = Mockito.mock(DelegateExecution.class);
        when(execution.getBusinessKey()).thenReturn("item-42");

        new SendRunProductionCommandDelegate(useCase).execute(execution);

        verify(useCase).sendRunProductionCommand("item-42");
    }
}
