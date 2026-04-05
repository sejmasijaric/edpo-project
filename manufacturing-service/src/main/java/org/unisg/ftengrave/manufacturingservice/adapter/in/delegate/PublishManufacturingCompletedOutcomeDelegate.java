package org.unisg.ftengrave.manufacturingservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.manufacturingservice.port.in.PublishManufacturingCompletedOutcomeUseCase;

@Component("PublishManufacturingCompletedOutcomeDelegate")
public class PublishManufacturingCompletedOutcomeDelegate implements JavaDelegate {

    private final PublishManufacturingCompletedOutcomeUseCase publishManufacturingCompletedOutcomeUseCase;

    public PublishManufacturingCompletedOutcomeDelegate(
            PublishManufacturingCompletedOutcomeUseCase publishManufacturingCompletedOutcomeUseCase) {
        this.publishManufacturingCompletedOutcomeUseCase = publishManufacturingCompletedOutcomeUseCase;
    }

    @Override
    public void execute(DelegateExecution execution) {
        publishManufacturingCompletedOutcomeUseCase.publish(execution.getBusinessKey());
    }
}
