package org.unisg.ftengrave.manufacturingservice.application;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.manufacturingservice.port.in.PublishUserTaskIssuedEventUseCase;
import org.unisg.ftengrave.manufacturingservice.port.out.PublishUserTaskIssuedEventPort;

@Service
@RequiredArgsConstructor
public class PublishUserTaskIssuedEventService implements PublishUserTaskIssuedEventUseCase {

    static final String ERROR_TASK = "error";
    static final String ENGRAVER_STATION = "engraver-station";
    static final String POLISHING_MACHINE_STATION = "polishing-machine-station";

    private static final Map<String, UserTaskDefinition> TASKS = Map.of(
            "Assess Item and Restore EGR Machine",
            new UserTaskDefinition("assess-item-and-restore-egr-machine-user-task-issued", ENGRAVER_STATION),
            "Assess Item and Restore PM",
            new UserTaskDefinition("assess-item-and-restore-pm-user-task-issued", POLISHING_MACHINE_STATION));

    private final PublishUserTaskIssuedEventPort publishUserTaskIssuedEventPort;

    @Override
    public void publish(String itemIdentifier, String taskName) {
        UserTaskDefinition definition = TASKS.get(taskName);
        if (definition == null) {
            return;
        }

        publishUserTaskIssuedEventPort.publish(
                itemIdentifier,
                definition.commandType(),
                taskName,
                ERROR_TASK,
                definition.stationName(),
                null);
    }

    private record UserTaskDefinition(String commandType, String stationName) {
    }
}
