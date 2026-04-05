package org.unisg.ftengrave.orderorchestrator.adapter.in.delegate;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.orderorchestrator.port.in.SendRunProductionCommandUseCase;

@Component("sendRunProductionCommandDelegate")
@RequiredArgsConstructor
public class SendRunProductionCommandDelegate implements JavaDelegate {

    private final SendRunProductionCommandUseCase sendRunProductionCommandUseCase;

    @Override
    public void execute(DelegateExecution execution) {
        sendRunProductionCommandUseCase.sendRunProductionCommand(execution.getBusinessKey());
    }
}
