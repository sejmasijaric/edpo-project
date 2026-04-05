package org.unisg.ftengrave.manufacturingservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.manufacturingservice.port.in.PublishManufacturingFailedOutcomeUseCase;

@Component("PublishManufacturingFailedOutcomeDelegate")
public class PublishManufacturingFailedOutcomeDelegate implements JavaDelegate {

    private final PublishManufacturingFailedOutcomeUseCase publishManufacturingFailedOutcomeUseCase;

    public PublishManufacturingFailedOutcomeDelegate(
            PublishManufacturingFailedOutcomeUseCase publishManufacturingFailedOutcomeUseCase) {
        this.publishManufacturingFailedOutcomeUseCase = publishManufacturingFailedOutcomeUseCase;
    }

    @Override
    public void execute(DelegateExecution execution) {
        publishManufacturingFailedOutcomeUseCase.publish(execution.getBusinessKey());
    }
}
