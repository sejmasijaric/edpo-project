package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.SortToShippingPublisher;

@Component("SortToShippingAdapter")
public class SortToShippingAdapter implements JavaDelegate {

    private final SortToShippingPublisher sortToShippingPublisher;

    public SortToShippingAdapter(SortToShippingPublisher sortToShippingPublisher) {
        this.sortToShippingPublisher = sortToShippingPublisher;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) throws Exception {
        // TODO: Implement
        System.out.println("ShippingSink");
        sortToShippingPublisher.publish();
    }
}
