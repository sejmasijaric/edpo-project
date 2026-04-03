package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.qcservice.application.MatchItemColorService;
import org.unisg.ftengrave.qcservice.domain.ItemColor;

class MatchItemColorAdapterTest {

    @Test
    void executeSetsPassedColorCheckFromMatchingService() {
        MatchItemColorService matchItemColorService = mock(MatchItemColorService.class);
        MatchItemColorAdapter adapter = new MatchItemColorAdapter(matchItemColorService);
        DelegateExecution delegateExecution = mock(DelegateExecution.class);

        when(delegateExecution.getVariable(MatchItemColorAdapter.DETECTED_COLOR_VARIABLE)).thenReturn(ItemColor.BLUE);
        when(delegateExecution.getVariable(MatchItemColorAdapter.TARGET_COLOR_VARIABLE)).thenReturn(ItemColor.BLUE);
        when(matchItemColorService.matches(ItemColor.BLUE, ItemColor.BLUE)).thenReturn(true);

        adapter.execute(delegateExecution);

        verify(matchItemColorService).matches(ItemColor.BLUE, ItemColor.BLUE);
        verify(delegateExecution).setVariable(MatchItemColorAdapter.PASSED_COLOR_CHECK_VARIABLE, true);
    }

    @Test
    void executeRaisesBpmnErrorWhenDetectedColorIsNone() {
        MatchItemColorService matchItemColorService = mock(MatchItemColorService.class);
        MatchItemColorAdapter adapter = new MatchItemColorAdapter(matchItemColorService);
        DelegateExecution delegateExecution = mock(DelegateExecution.class);

        when(delegateExecution.getVariable(MatchItemColorAdapter.DETECTED_COLOR_VARIABLE)).thenReturn(ItemColor.NONE);

        assertThatThrownBy(() -> adapter.execute(delegateExecution))
                .isInstanceOf(BpmnError.class)
                .extracting(throwable -> ((BpmnError) throwable).getErrorCode())
                .isEqualTo(MatchItemColorAdapter.COLOR_DETECTION_FAILED_ERROR);
    }
}
