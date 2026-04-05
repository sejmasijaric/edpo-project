package org.unisg.ftengrave.orderorchestrator.adapter.in.delegate;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;
import org.unisg.ftengrave.orderorchestrator.port.in.SendPerformQcCommandUseCase;

@Component("sendPerformQcCommandDelegate")
@RequiredArgsConstructor
public class SendPerformQcCommandDelegate implements JavaDelegate {

    private final SendPerformQcCommandUseCase sendPerformQcCommandUseCase;

    @Override
    public void execute(DelegateExecution execution) {
        sendPerformQcCommandUseCase.sendPerformQcCommand(
                execution.getBusinessKey(),
                ItemColor.valueOf(String.valueOf(execution.getVariable("targetColor"))));
    }
}
