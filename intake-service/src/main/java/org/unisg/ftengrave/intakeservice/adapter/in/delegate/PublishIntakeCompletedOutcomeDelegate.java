package org.unisg.ftengrave.intakeservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.intakeservice.port.in.PublishIntakeCompletedOutcomeUseCase;

@Component("PublishIntakeCompletedOutcomeDelegate")
public class PublishIntakeCompletedOutcomeDelegate implements JavaDelegate {

    private final PublishIntakeCompletedOutcomeUseCase publishIntakeCompletedOutcomeUseCase;

    public PublishIntakeCompletedOutcomeDelegate(
            PublishIntakeCompletedOutcomeUseCase publishIntakeCompletedOutcomeUseCase) {
        this.publishIntakeCompletedOutcomeUseCase = publishIntakeCompletedOutcomeUseCase;
    }

    @Override
    public void execute(DelegateExecution execution) {
        publishIntakeCompletedOutcomeUseCase.publish(execution.getBusinessKey());
    }
}
