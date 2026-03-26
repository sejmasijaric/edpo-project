package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.SortToRejectPublisher;

@Component("SortToRejectionAdapter")
public class SortToRejectionAdapter implements JavaDelegate {

    private final SortToRejectPublisher sortToRejectPublisher;

    public SortToRejectionAdapter(SortToRejectPublisher sortToRejectPublisher) {
        this.sortToRejectPublisher = sortToRejectPublisher;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        sortToRejectPublisher.publish();
    }
}
