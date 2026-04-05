package org.unisg.ftengrave.intakeservice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.intakeservice.domain.ItemColor;
import org.unisg.ftengrave.intakeservice.port.in.PublishInsertItemIntoIntakeCommandUseCase;
import org.unisg.ftengrave.intakeservice.port.out.PublishInsertItemIntoIntakeCommandPort;

@Service
@RequiredArgsConstructor
public class PublishInsertItemIntoIntakeCommandService implements PublishInsertItemIntoIntakeCommandUseCase {

    private final PublishInsertItemIntoIntakeCommandPort publishInsertItemIntoIntakeCommandPort;

    @Override
    public void publish(String itemIdentifier, String itemColor) {
        publishInsertItemIntoIntakeCommandPort.publish(itemIdentifier, ItemColor.valueOf(itemColor));
    }
}
