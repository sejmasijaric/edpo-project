package org.unisg.ftengrave.intakeservice.application;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.unisg.ftengrave.intakeservice.domain.ItemColor;
import org.unisg.ftengrave.intakeservice.port.out.PublishInsertItemIntoIntakeCommandPort;

import static org.mockito.Mockito.verify;

class PublishInsertItemIntoIntakeEventServiceTest {

    @Test
    void publishDelegatesToPortWithResolvedItemColor() {
        PublishInsertItemIntoIntakeCommandPort port = Mockito.mock(PublishInsertItemIntoIntakeCommandPort.class);

        new PublishInsertItemIntoIntakeCommandService(port).publish("item-42", "RED");

        verify(port).publish("item-42", ItemColor.RED);
    }
}
