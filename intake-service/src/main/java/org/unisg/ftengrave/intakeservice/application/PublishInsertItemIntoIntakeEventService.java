package org.unisg.ftengrave.intakeservice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.intakeservice.domain.ItemColor;
import org.unisg.ftengrave.intakeservice.port.in.PublishInsertItemIntoIntakeEventUseCase;
import org.unisg.ftengrave.intakeservice.port.out.PublishInsertItemIntoIntakeEventPort;

@Service
@RequiredArgsConstructor
public class PublishInsertItemIntoIntakeEventService implements PublishInsertItemIntoIntakeEventUseCase {

    private final PublishInsertItemIntoIntakeEventPort publishInsertItemIntoIntakeEventPort;

    @Override
    public void publish(String itemIdentifier, String itemColor) {
        publishInsertItemIntoIntakeEventPort.publish(itemIdentifier, ItemColor.valueOf(itemColor));
    }
}
