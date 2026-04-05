package org.unisg.ftengrave.intakeservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.intakeservice.port.in.PublishInsertItemIntoIntakeEventUseCase;

@Component("PublishInsertItemIntoIntakeEventDelegate")
public class PublishInsertItemIntoIntakeEventDelegate implements JavaDelegate {

    private final PublishInsertItemIntoIntakeEventUseCase publishInsertItemIntoIntakeEventUseCase;

    public PublishInsertItemIntoIntakeEventDelegate(
            PublishInsertItemIntoIntakeEventUseCase publishInsertItemIntoIntakeEventUseCase) {
        this.publishInsertItemIntoIntakeEventUseCase = publishInsertItemIntoIntakeEventUseCase;
    }

    @Override
    public void execute(DelegateExecution execution) {
        publishInsertItemIntoIntakeEventUseCase.publish(
                execution.getBusinessKey(),
                String.valueOf(execution.getVariable("targetColor")));
    }
}
