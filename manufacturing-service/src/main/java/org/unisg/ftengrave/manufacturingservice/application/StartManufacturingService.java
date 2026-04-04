package org.unisg.ftengrave.manufacturingservice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.manufacturingservice.port.in.StartManufacturingUseCase;
import org.unisg.ftengrave.manufacturingservice.port.out.CorrelateMessagePort;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class StartManufacturingService implements StartManufacturingUseCase {

    static final String START_MANUFACTURING_MESSAGE = "StartProductionMessage";

    private final CorrelateMessagePort correlateMessagePort;

    @Override
    public boolean startManufacturing(String itemIdentifier) {
        return correlateMessagePort.correlateMessage(
                START_MANUFACTURING_MESSAGE,
                itemIdentifier,
                Map.of("itemIdentifier", itemIdentifier))
                != null;
    }
}
