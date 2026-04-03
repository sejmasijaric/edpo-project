package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.qcservice.domain.ItemColor;
import org.unisg.ftengrave.qcservice.port.in.MatchItemColorUseCase;

class MatchDetectedColorToOrderDelegateTest {

    @Test
    void executeSetsPassedColorCheckFromMatchingService() {
        MatchItemColorUseCase matchItemColorUseCase = mock(MatchItemColorUseCase.class);
        MatchDetectedColorToOrderDelegate delegate = new MatchDetectedColorToOrderDelegate(matchItemColorUseCase);
        DelegateExecution delegateExecution = mock(DelegateExecution.class);

        when(delegateExecution.getVariable(MatchDetectedColorToOrderDelegate.DETECTED_COLOR_VARIABLE)).thenReturn(ItemColor.BLUE);
        when(delegateExecution.getVariable(MatchDetectedColorToOrderDelegate.TARGET_COLOR_VARIABLE)).thenReturn(ItemColor.BLUE);
        when(matchItemColorUseCase.matches(ItemColor.BLUE, ItemColor.BLUE)).thenReturn(true);

        delegate.execute(delegateExecution);

        verify(matchItemColorUseCase).matches(ItemColor.BLUE, ItemColor.BLUE);
        verify(delegateExecution).setVariable(MatchDetectedColorToOrderDelegate.PASSED_COLOR_CHECK_VARIABLE, true);
    }

    @Test
    void executeRaisesBpmnErrorWhenDetectedColorIsNone() {
        MatchItemColorUseCase matchItemColorUseCase = mock(MatchItemColorUseCase.class);
        MatchDetectedColorToOrderDelegate delegate = new MatchDetectedColorToOrderDelegate(matchItemColorUseCase);
        DelegateExecution delegateExecution = mock(DelegateExecution.class);

        when(delegateExecution.getVariable(MatchDetectedColorToOrderDelegate.DETECTED_COLOR_VARIABLE)).thenReturn(ItemColor.NONE);

        assertThatThrownBy(() -> delegate.execute(delegateExecution))
                .isInstanceOf(BpmnError.class)
                .extracting(throwable -> ((BpmnError) throwable).getErrorCode())
                .isEqualTo(MatchDetectedColorToOrderDelegate.COLOR_DETECTION_FAILED_ERROR);
    }
}
