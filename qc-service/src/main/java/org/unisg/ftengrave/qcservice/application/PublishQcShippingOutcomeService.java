package org.unisg.ftengrave.qcservice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.qcservice.port.in.PublishQcShippingOutcomeUseCase;
import org.unisg.ftengrave.qcservice.port.out.PublishQcShippingOutcomePort;

@Service
@RequiredArgsConstructor
public class PublishQcShippingOutcomeService implements PublishQcShippingOutcomeUseCase {

    private final PublishQcShippingOutcomePort publishQcShippingOutcomePort;

    @Override
    public void publish(String itemIdentifier) {
        publishQcShippingOutcomePort.publish(itemIdentifier);
    }
}
