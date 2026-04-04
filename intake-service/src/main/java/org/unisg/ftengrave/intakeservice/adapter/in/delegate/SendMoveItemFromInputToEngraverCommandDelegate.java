package org.unisg.ftengrave.intakeservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.intakeservice.port.in.SendMoveItemFromInputToEngraverUseCase;

@Component("SendMoveItemFromInputToEngraverCommandDelegate")
public class SendMoveItemFromInputToEngraverCommandDelegate implements JavaDelegate {

    private final SendMoveItemFromInputToEngraverUseCase sendMoveItemFromInputToEngraverUseCase;

    public SendMoveItemFromInputToEngraverCommandDelegate(
            SendMoveItemFromInputToEngraverUseCase sendMoveItemFromInputToEngraverUseCase) {
        this.sendMoveItemFromInputToEngraverUseCase = sendMoveItemFromInputToEngraverUseCase;
    }

    @Override
    public void execute(DelegateExecution execution) {
        sendMoveItemFromInputToEngraverUseCase.send(String.valueOf(execution.getVariable("itemIdentifier")));
    }
}
