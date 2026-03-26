package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.SortToRetryPublisher;

@Component("SortToRetryAdapter")
public class SortToRetryAdapter implements JavaDelegate {

    private final SortToRetryPublisher sortToRetryPublisher;

    public SortToRetryAdapter(SortToRetryPublisher sortToRetryPublisher) {
        this.sortToRetryPublisher = sortToRetryPublisher;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        sortToRetryPublisher.publish();
    }
}
