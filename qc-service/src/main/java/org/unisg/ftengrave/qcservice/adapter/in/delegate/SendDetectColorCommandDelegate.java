package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.port.in.RequestColorDetectionUseCase;

@Component("SendDetectColorCommandDelegate")
public class SendDetectColorCommandDelegate implements JavaDelegate {

    private final RequestColorDetectionUseCase requestColorDetectionUseCase;

    public SendDetectColorCommandDelegate(RequestColorDetectionUseCase requestColorDetectionUseCase) {
        this.requestColorDetectionUseCase = requestColorDetectionUseCase;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) {
        requestColorDetectionUseCase.requestColorDetection();
    }
}
