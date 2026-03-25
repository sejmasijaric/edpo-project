package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

@Component("DetectColorAdapter")
public class DetectColorAdapter implements JavaDelegate {
    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {

//        System.out.println("sleeping 40s");
//        Thread.sleep(40000);

        // TODO: Implement
        System.out.println("DetectingColor");

        Boolean failed = false;
        if (failed) {
            //delegateExecution.setVariable("colorCheckMessage", "No color could be detected");
            throw new BpmnError("COLOR_DETECTION_FAILED", "No color detected");
        }

        delegateExecution.setVariable("passedColorCheck", (boolean)true);
    }
}
