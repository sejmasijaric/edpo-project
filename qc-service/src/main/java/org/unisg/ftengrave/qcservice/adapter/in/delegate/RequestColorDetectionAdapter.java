package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.RequestColorDetectionPublisher;

@Component("RequestColorDetectionAdapter")
public class RequestColorDetectionAdapter implements JavaDelegate {

    private final RequestColorDetectionPublisher requestColorDetectionPublisher;

    public RequestColorDetectionAdapter(RequestColorDetectionPublisher requestColorDetectionPublisher) {
        this.requestColorDetectionPublisher = requestColorDetectionPublisher;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) {
        requestColorDetectionPublisher.publish();
    }
}
