package org.unisg.ftengrave.manufacturingservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.manufacturingservice.port.in.SendRunEngravingCommandUseCase;

@Component("SendRunEngravingCommandDelegate")
public class SendRunEngravingCommandDelegate implements JavaDelegate {

    private final SendRunEngravingCommandUseCase sendRunEngravingCommandUseCase;

    public SendRunEngravingCommandDelegate(SendRunEngravingCommandUseCase sendRunEngravingCommandUseCase) {
        this.sendRunEngravingCommandUseCase = sendRunEngravingCommandUseCase;
    }

    @Override
    public void execute(DelegateExecution execution) {
        sendRunEngravingCommandUseCase.send(String.valueOf(execution.getVariable("itemIdentifier")));
    }
}
