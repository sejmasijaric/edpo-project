package org.unisg.ftengrave.qcservice.application;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.qcservice.port.in.PublishUserTaskIssuedEventUseCase;
import org.unisg.ftengrave.qcservice.port.out.PublishUserTaskIssuedEventPort;

@Service
@RequiredArgsConstructor
public class PublishUserTaskIssuedEventService implements PublishUserTaskIssuedEventUseCase {

    static final String ERROR_TASK = "error";
    static final String NORMAL_TASK = "normal";
    static final String QUALITY_CONTROL_STATION = "quality-control-station";

    private static final Map<String, UserTaskDefinition> TASKS = Map.of(
            "Resolve Issue and Restore Item Position",
            new UserTaskDefinition(
                    "resolve-issue-and-restore-item-position-user-task-issued",
                    ERROR_TASK),
            "Check Quality",
            new UserTaskDefinition("check-quality-user-task-issued", NORMAL_TASK),
            "Restore Item Position",
            new UserTaskDefinition("restore-item-position-user-task-issued", ERROR_TASK));

    private final PublishUserTaskIssuedEventPort publishUserTaskIssuedEventPort;

    @Override
    public void publish(String itemIdentifier, String taskName, String targetColor) {
        UserTaskDefinition definition = TASKS.get(taskName);
        if (definition == null) {
            return;
        }

        publishUserTaskIssuedEventPort.publish(
                itemIdentifier,
                definition.commandType(),
                taskName,
                definition.taskCategory(),
                QUALITY_CONTROL_STATION,
                targetColor);
    }

    private record UserTaskDefinition(String commandType, String taskCategory) {
    }
}
