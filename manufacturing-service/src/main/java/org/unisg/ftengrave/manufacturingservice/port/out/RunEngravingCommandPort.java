package org.unisg.ftengrave.manufacturingservice.port.out;

public interface RunEngravingCommandPort {

    void publish(String itemIdentifier);
}
