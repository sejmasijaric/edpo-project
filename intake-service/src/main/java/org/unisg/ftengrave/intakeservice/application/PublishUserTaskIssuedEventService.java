package org.unisg.ftengrave.intakeservice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.intakeservice.port.in.PublishUserTaskIssuedEventUseCase;
import org.unisg.ftengrave.intakeservice.port.out.PublishUserTaskIssuedEventPort;

@Service
@RequiredArgsConstructor
public class PublishUserTaskIssuedEventService implements PublishUserTaskIssuedEventUseCase {

    static final String ERROR_TASK = "error";
    static final String INTAKE_STATION = "item-intake-station";
    static final String RESOLVE_ISSUE_AND_REPLACE_ITEM_EVENT = "resolve-issue-and-replace-item-user-task-issued";

    private final PublishUserTaskIssuedEventPort publishUserTaskIssuedEventPort;

    @Override
    public void publish(String itemIdentifier, String taskName, String targetColor) {
        publishUserTaskIssuedEventPort.publish(
                itemIdentifier,
                RESOLVE_ISSUE_AND_REPLACE_ITEM_EVENT,
                taskName,
                ERROR_TASK,
                INTAKE_STATION,
                targetColor);
    }
}
