package org.unisg.ftengrave.manufacturingservice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.manufacturingservice.port.in.PublishManufacturingFailedOutcomeUseCase;
import org.unisg.ftengrave.manufacturingservice.port.out.PublishManufacturingFailedOutcomePort;

@Service
@RequiredArgsConstructor
public class PublishManufacturingFailedOutcomeService implements PublishManufacturingFailedOutcomeUseCase {

    private final PublishManufacturingFailedOutcomePort publishManufacturingFailedOutcomePort;

    @Override
    public void publish(String itemIdentifier) {
        publishManufacturingFailedOutcomePort.publish(itemIdentifier);
    }
}
