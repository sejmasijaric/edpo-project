package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.port.in.SortToShippingUseCase;

@Component("SortToShippingAdapter")
public class SortToShippingAdapter implements JavaDelegate {

    private final SortToShippingUseCase sortToShippingUseCase;

    public SortToShippingAdapter(SortToShippingUseCase sortToShippingUseCase) {
        this.sortToShippingUseCase = sortToShippingUseCase;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        sortToShippingUseCase.sortToShipping();
    }
}
