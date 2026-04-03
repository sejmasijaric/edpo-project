package org.unisg.ftengrave.orderorchestrator.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;

class InitializeOrderContextDelegateTest {

    @Test
    void executeAllowsPlaceholderDelegateToRun() throws Exception {
        InitializeOrderContextDelegate delegate = new InitializeOrderContextDelegate();

        DelegateExecution execution = mock(DelegateExecution.class);

        delegate.execute(execution);
    }
}
