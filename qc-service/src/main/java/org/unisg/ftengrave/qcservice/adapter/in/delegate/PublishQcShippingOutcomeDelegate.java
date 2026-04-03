package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.port.in.PublishQcShippingOutcomeUseCase;

@Component("PublishQcShippingOutcomeDelegate")
@RequiredArgsConstructor
public class PublishQcShippingOutcomeDelegate implements JavaDelegate {

    private final PublishQcShippingOutcomeUseCase publishQcShippingOutcomeUseCase;

    @Override
    public void execute(DelegateExecution execution) {
        publishQcShippingOutcomeUseCase.publish(execution.getBusinessKey());
    }
}
