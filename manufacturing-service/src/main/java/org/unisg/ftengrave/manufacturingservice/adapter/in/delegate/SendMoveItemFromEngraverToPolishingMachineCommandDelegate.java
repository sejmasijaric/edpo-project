package org.unisg.ftengrave.manufacturingservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.manufacturingservice.port.in.SendMoveItemFromEngraverToPolishingMachineCommandUseCase;

@Component("SendMoveItemFromEngraverToPolishingMachineCommandDelegate")
public class SendMoveItemFromEngraverToPolishingMachineCommandDelegate implements JavaDelegate {

    private final SendMoveItemFromEngraverToPolishingMachineCommandUseCase sendMoveItemFromEngraverToPolishingMachineCommandUseCase;

    public SendMoveItemFromEngraverToPolishingMachineCommandDelegate(
            SendMoveItemFromEngraverToPolishingMachineCommandUseCase sendMoveItemFromEngraverToPolishingMachineCommandUseCase) {
        this.sendMoveItemFromEngraverToPolishingMachineCommandUseCase = sendMoveItemFromEngraverToPolishingMachineCommandUseCase;
    }

    @Override
    public void execute(DelegateExecution execution) {
        sendMoveItemFromEngraverToPolishingMachineCommandUseCase.send(String.valueOf(execution.getVariable("itemIdentifier")));
    }
}
