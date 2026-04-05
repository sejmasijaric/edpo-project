package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.port.in.PublishQcRejectionOutcomeUseCase;

@Component("PublishQcRejectionOutcomeDelegate")
@RequiredArgsConstructor
public class PublishQcRejectionOutcomeDelegate implements JavaDelegate {

    private final PublishQcRejectionOutcomeUseCase publishQcRejectionOutcomeUseCase;

    @Override
    public void execute(DelegateExecution execution) {
        publishQcRejectionOutcomeUseCase.publish(execution.getBusinessKey());
    }
}
