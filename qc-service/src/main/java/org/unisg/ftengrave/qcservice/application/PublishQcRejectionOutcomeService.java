package org.unisg.ftengrave.qcservice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.qcservice.port.in.PublishQcRejectionOutcomeUseCase;
import org.unisg.ftengrave.qcservice.port.out.PublishQcRejectionOutcomePort;

@Service
@RequiredArgsConstructor
public class PublishQcRejectionOutcomeService implements PublishQcRejectionOutcomeUseCase {

    private final PublishQcRejectionOutcomePort publishQcRejectionOutcomePort;

    @Override
    public void publish(String itemIdentifier) {
        publishQcRejectionOutcomePort.publish(itemIdentifier);
    }
}
