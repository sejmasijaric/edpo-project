package org.unisg.ftengrave.orderorchestrator.adapter.in.delegate;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;
import org.unisg.ftengrave.orderorchestrator.port.in.SendRunIntakeCommandUseCase;

@Component("sendRunIntakeCommandDelegate")
@RequiredArgsConstructor
public class SendRunIntakeCommandDelegate implements JavaDelegate {

    private final SendRunIntakeCommandUseCase sendRunIntakeCommandUseCase;

    @Override
    public void execute(DelegateExecution execution) {
        sendRunIntakeCommandUseCase.sendRunIntakeCommand(
                execution.getBusinessKey(),
                ItemColor.valueOf(String.valueOf(execution.getVariable("targetColor"))));
    }
}
