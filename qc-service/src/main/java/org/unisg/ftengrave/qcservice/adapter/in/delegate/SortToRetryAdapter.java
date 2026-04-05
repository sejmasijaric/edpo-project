package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.port.in.SortToRetryUseCase;

@Component("SortToRetryAdapter")
public class SortToRetryAdapter implements JavaDelegate {

    private final SortToRetryUseCase sortToRetryUseCase;

    public SortToRetryAdapter(SortToRetryUseCase sortToRetryUseCase) {
        this.sortToRetryUseCase = sortToRetryUseCase;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        sortToRetryUseCase.sortToRetry();
    }
}
