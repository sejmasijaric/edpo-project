package org.unisg.ftengrave.manufacturingservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.manufacturingservice.port.in.SendRunPolishingCommandUseCase;

@Component("SendRunPolishingCommandDelegate")
public class SendRunPolishingCommandDelegate implements JavaDelegate {

    private final SendRunPolishingCommandUseCase sendRunPolishingCommandUseCase;

    public SendRunPolishingCommandDelegate(SendRunPolishingCommandUseCase sendRunPolishingCommandUseCase) {
        this.sendRunPolishingCommandUseCase = sendRunPolishingCommandUseCase;
    }

    @Override
    public void execute(DelegateExecution execution) {
        sendRunPolishingCommandUseCase.send(String.valueOf(execution.getVariable("itemIdentifier")));
    }
}
