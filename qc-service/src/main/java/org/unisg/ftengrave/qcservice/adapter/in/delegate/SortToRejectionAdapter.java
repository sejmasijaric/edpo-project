package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.port.in.SortToRejectUseCase;

@Component("SortToRejectionAdapter")
public class SortToRejectionAdapter implements JavaDelegate {

    private final SortToRejectUseCase sortToRejectUseCase;

    public SortToRejectionAdapter(SortToRejectUseCase sortToRejectUseCase) {
        this.sortToRejectUseCase = sortToRejectUseCase;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        sortToRejectUseCase.sortToReject();
    }
}
