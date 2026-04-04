package org.unisg.ftengrave.manufacturingservice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.manufacturingservice.port.in.SendRunPolishingCommandUseCase;
import org.unisg.ftengrave.manufacturingservice.port.out.RunPolishingCommandPort;

@Service
@RequiredArgsConstructor
public class SendRunPolishingCommandService implements SendRunPolishingCommandUseCase {

    private final RunPolishingCommandPort runPolishingCommandPort;

    @Override
    public void send(String itemIdentifier) {
        runPolishingCommandPort.publish(itemIdentifier);
    }
}
