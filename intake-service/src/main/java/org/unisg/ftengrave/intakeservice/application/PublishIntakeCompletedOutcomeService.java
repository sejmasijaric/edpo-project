package org.unisg.ftengrave.intakeservice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.intakeservice.port.in.PublishIntakeCompletedOutcomeUseCase;
import org.unisg.ftengrave.intakeservice.port.out.PublishIntakeCompletedOutcomePort;

@Service
@RequiredArgsConstructor
public class PublishIntakeCompletedOutcomeService implements PublishIntakeCompletedOutcomeUseCase {

    private final PublishIntakeCompletedOutcomePort publishIntakeCompletedOutcomePort;

    @Override
    public void publish(String itemIdentifier) {
        publishIntakeCompletedOutcomePort.publish(itemIdentifier);
    }
}
