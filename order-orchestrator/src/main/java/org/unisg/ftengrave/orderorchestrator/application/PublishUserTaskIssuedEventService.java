package org.unisg.ftengrave.orderorchestrator.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.orderorchestrator.port.in.PublishUserTaskIssuedEventUseCase;
import org.unisg.ftengrave.orderorchestrator.port.out.PublishUserTaskIssuedEventPort;

@Service
@RequiredArgsConstructor
public class PublishUserTaskIssuedEventService implements PublishUserTaskIssuedEventUseCase {

    static final String ERROR_TASK = "error";
    static final String FACTORY_STATION = "factory-station";
    static final String REMOVE_ITEM_FROM_FACTORY_EVENT = "remove-item-from-factory-user-task-issued";

    private final PublishUserTaskIssuedEventPort publishUserTaskIssuedEventPort;

    @Override
    public void publish(String itemIdentifier, String taskName, String targetColor) {
        publishUserTaskIssuedEventPort.publish(
                itemIdentifier,
                REMOVE_ITEM_FROM_FACTORY_EVENT,
                taskName,
                ERROR_TASK,
                FACTORY_STATION,
                targetColor);
    }
}
