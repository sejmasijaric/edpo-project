package org.unisg.ftengrave.manufacturingservice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.manufacturingservice.port.in.PublishManufacturingCompletedOutcomeUseCase;
import org.unisg.ftengrave.manufacturingservice.port.out.PublishManufacturingCompletedOutcomePort;

@Service
@RequiredArgsConstructor
public class PublishManufacturingCompletedOutcomeService implements PublishManufacturingCompletedOutcomeUseCase {

    private final PublishManufacturingCompletedOutcomePort publishManufacturingCompletedOutcomePort;

    @Override
    public void publish(String itemIdentifier) {
        publishManufacturingCompletedOutcomePort.publish(itemIdentifier);
    }
}
