package org.unisg.ftengrave.orderorchestrator.application;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.orderorchestrator.port.in.HandleServiceOutcomeEventUseCase;
import org.unisg.ftengrave.orderorchestrator.port.out.CorrelateMessagePort;

@Service
@RequiredArgsConstructor
public class HandleServiceOutcomeEventService implements HandleServiceOutcomeEventUseCase {

    static final String INTAKE_COMPLETED_OUTCOME_MESSAGE = "IntakeCompletedOutcomeMessage";
    static final String MANUFACTURING_COMPLETED_OUTCOME_MESSAGE = "ManufacturingCompletedOutcomeMessage";
    static final String MANUFACTURING_FAILED_OUTCOME_MESSAGE = "ManufacturingFailedOutcomeMessage";
    static final String QC_SHIPPING_OUTCOME_MESSAGE = "QcShippingOutcomeMessage";
    static final String QC_REJECTION_OUTCOME_MESSAGE = "QcRejectionOutcomeMessage";

    static final String INTAKE_COMPLETED_OUTCOME_TYPE = "intake-completed";
    static final String MANUFACTURING_COMPLETED_OUTCOME_TYPE = "manufacturing-completed";
    static final String MANUFACTURING_FAILED_OUTCOME_TYPE = "manufacturing-failed";
    static final String SHIPPING_OUTCOME_TYPE = "qc-shipping";
    static final String REJECTION_OUTCOME_TYPE = "qc-rejection";

    private final CorrelateMessagePort correlateMessagePort;

    @Override
    public void handle(String itemIdentifier, String outcomeType) {
        String messageName = switch (outcomeType) {
            case INTAKE_COMPLETED_OUTCOME_TYPE -> INTAKE_COMPLETED_OUTCOME_MESSAGE;
            case MANUFACTURING_COMPLETED_OUTCOME_TYPE -> MANUFACTURING_COMPLETED_OUTCOME_MESSAGE;
            case MANUFACTURING_FAILED_OUTCOME_TYPE -> MANUFACTURING_FAILED_OUTCOME_MESSAGE;
            case SHIPPING_OUTCOME_TYPE -> QC_SHIPPING_OUTCOME_MESSAGE;
            case REJECTION_OUTCOME_TYPE -> QC_REJECTION_OUTCOME_MESSAGE;
            default -> null;
        };

        if (messageName != null) {
            correlateMessagePort.correlateMessage(messageName, itemIdentifier, null);
        }
    }
}
