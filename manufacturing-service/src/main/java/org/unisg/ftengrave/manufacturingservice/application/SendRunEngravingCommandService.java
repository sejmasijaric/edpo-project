package org.unisg.ftengrave.manufacturingservice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.manufacturingservice.port.in.SendRunEngravingCommandUseCase;
import org.unisg.ftengrave.manufacturingservice.port.out.RunEngravingCommandPort;

@Service
@RequiredArgsConstructor
public class SendRunEngravingCommandService implements SendRunEngravingCommandUseCase {

    private final RunEngravingCommandPort runEngravingCommandPort;

    @Override
    public void send(String itemIdentifier) {
        runEngravingCommandPort.publish(itemIdentifier);
    }
}
