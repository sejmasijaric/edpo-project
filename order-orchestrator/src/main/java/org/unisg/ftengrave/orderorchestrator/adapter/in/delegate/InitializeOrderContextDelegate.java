package org.unisg.ftengrave.orderorchestrator.adapter.in.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InitializeOrderContextDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("Initializing order orchestration context for orderIdentifier={}",
                execution.getVariable("orderIdentifier"));
    }
}
