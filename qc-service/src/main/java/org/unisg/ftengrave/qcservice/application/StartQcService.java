package org.unisg.ftengrave.qcservice.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.qcservice.domain.ItemColor;
import org.unisg.ftengrave.qcservice.port.in.StartQcUseCase;
import org.unisg.ftengrave.qcservice.port.out.CorrelateMessagePort;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class StartQcService implements StartQcUseCase {

    static final String START_QC_MESSAGE = "StartQcMessage";

    private final CorrelateMessagePort correlateMessagePort;

    @Override
    public boolean startQc(String itemIdentifier, ItemColor targetColor) {
        return correlateMessagePort.correlateMessage(
                START_QC_MESSAGE,
                itemIdentifier,
                Map.of(
                        "itemIdentifier", itemIdentifier,
                        "targetColor", targetColor))
                != null;
    }
}
