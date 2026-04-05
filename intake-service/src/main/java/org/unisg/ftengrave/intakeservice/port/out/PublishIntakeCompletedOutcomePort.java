package org.unisg.ftengrave.intakeservice.port.out;

public interface PublishIntakeCompletedOutcomePort {

    void publish(String itemIdentifier);
}
