package org.unisg.ftengrave.intakeservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.intakeservice.port.in.PublishInsertItemIntoIntakeCommandUseCase;

@Component("PublishInsertItemIntoIntakeCommandDelegate")
public class PublishInsertItemIntoIntakeCommandDelegate implements JavaDelegate {

    private final PublishInsertItemIntoIntakeCommandUseCase publishInsertItemIntoIntakeCommandUseCase;

    public PublishInsertItemIntoIntakeCommandDelegate(
            PublishInsertItemIntoIntakeCommandUseCase publishInsertItemIntoIntakeCommandUseCase) {
        this.publishInsertItemIntoIntakeCommandUseCase = publishInsertItemIntoIntakeCommandUseCase;
    }

    @Override
    public void execute(DelegateExecution execution) {
        publishInsertItemIntoIntakeCommandUseCase.publish(
                execution.getBusinessKey(),
                String.valueOf(execution.getVariable("targetColor")));
    }
}
