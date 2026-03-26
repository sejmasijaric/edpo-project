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


        // BEGIN GARBAGE
        // TODO: Remove Garbage
        System.out.println("DetectingColor");

        Boolean failed = false;
        if (failed) {
            //delegateExecution.setVariable("colorCheckMessage", "No color could be detected");
            throw new BpmnError("COLOR_DETECTION_FAILED", "No color detected");
        }

        delegateExecution.setVariable("passedColorCheck", (boolean)true);
        // END GARBAGE

        requestColorDetectionPublisher.publish();
    }
}
