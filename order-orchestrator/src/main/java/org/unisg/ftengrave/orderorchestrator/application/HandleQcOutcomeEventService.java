package org.unisg.ftengrave.orderorchestrator.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.orderorchestrator.port.in.HandleQcOutcomeEventUseCase;
import org.unisg.ftengrave.orderorchestrator.port.out.CorrelateMessagePort;

@Service
@RequiredArgsConstructor
public class HandleQcOutcomeEventService implements HandleQcOutcomeEventUseCase {

    static final String QC_SHIPPING_OUTCOME_MESSAGE = "QcShippingOutcomeMessage";
    static final String QC_REJECTION_OUTCOME_MESSAGE = "QcRejectionOutcomeMessage";
    static final String SHIPPING_OUTCOME_TYPE = "qc-shipping";
    static final String REJECTION_OUTCOME_TYPE = "qc-rejection";

    private final CorrelateMessagePort correlateMessagePort;

    @Override
    public void handle(String itemIdentifier, String outcomeType) {
        String messageName = switch (outcomeType) {
            case SHIPPING_OUTCOME_TYPE -> QC_SHIPPING_OUTCOME_MESSAGE;
            case REJECTION_OUTCOME_TYPE -> QC_REJECTION_OUTCOME_MESSAGE;
            default -> null;
        };

        if (messageName != null) {
            correlateMessagePort.correlateMessage(messageName, itemIdentifier, null);
        }
    }
}
