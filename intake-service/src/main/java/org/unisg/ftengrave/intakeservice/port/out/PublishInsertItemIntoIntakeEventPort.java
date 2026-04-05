package org.unisg.ftengrave.intakeservice.port.out;

import org.unisg.ftengrave.intakeservice.domain.ItemColor;

public interface PublishInsertItemIntoIntakeEventPort {

    void publish(String itemIdentifier, ItemColor itemColor);
}
