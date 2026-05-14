package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateTask;
import org.camunda.bpm.engine.delegate.TaskListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.port.in.PublishUserTaskIssuedEventUseCase;

@Component("PublishUserTaskIssuedEventDelegate")
public class PublishUserTaskIssuedEventDelegate implements TaskListener {

    private final PublishUserTaskIssuedEventUseCase publishUserTaskIssuedEventUseCase;

    public PublishUserTaskIssuedEventDelegate(PublishUserTaskIssuedEventUseCase publishUserTaskIssuedEventUseCase) {
        this.publishUserTaskIssuedEventUseCase = publishUserTaskIssuedEventUseCase;
    }

    @Override
    public void notify(DelegateTask delegateTask) {
        publishUserTaskIssuedEventUseCase.publish(
                delegateTask.getExecution().getBusinessKey(),
                delegateTask.getName(),
                targetColor(delegateTask));
    }

    private String targetColor(DelegateTask delegateTask) {
        Object targetColor = delegateTask.getExecution().getVariable("targetColor");
        return targetColor == null ? null : String.valueOf(targetColor);
    }
}
