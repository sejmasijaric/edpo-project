package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.domain.ItemColor;
import org.unisg.ftengrave.qcservice.port.in.MatchItemColorUseCase;

@Component("MatchDetectedColorToOrderDelegate")
public class MatchDetectedColorToOrderDelegate implements JavaDelegate {

    static final String DETECTED_COLOR_VARIABLE = "detected-color";
    static final String TARGET_COLOR_VARIABLE = "targetColor";
    static final String PASSED_COLOR_CHECK_VARIABLE = "passedColorCheck";
    static final String COLOR_DETECTION_FAILED_ERROR = "COLOR_DETECTION_FAILED";

    private final MatchItemColorUseCase matchItemColorUseCase;

    public MatchDetectedColorToOrderDelegate(MatchItemColorUseCase matchItemColorUseCase) {
        this.matchItemColorUseCase = matchItemColorUseCase;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) {
        ItemColor detectedColor = getRequiredColor(delegateExecution, DETECTED_COLOR_VARIABLE);
        if (detectedColor == ItemColor.NONE) {
            throw new BpmnError(COLOR_DETECTION_FAILED_ERROR, "No color detected");
        }

        ItemColor targetColor = getRequiredColor(delegateExecution, TARGET_COLOR_VARIABLE);
        boolean passedColorCheck = matchItemColorUseCase.matches(detectedColor, targetColor);
        delegateExecution.setVariable(PASSED_COLOR_CHECK_VARIABLE, passedColorCheck);
    }

    private ItemColor getRequiredColor(DelegateExecution delegateExecution, String variableName) {
        Object rawValue = delegateExecution.getVariable(variableName);

        if (rawValue instanceof ItemColor color) {
            return color;
        }
        if (rawValue instanceof String colorName) {
            ItemColor color = ItemColor.fromExternalValue(colorName);
            if (color != null) {
                return color;
            }
        }
        if (rawValue == null) {
            return ItemColor.NONE;
        }
        throw new IllegalStateException("Process variable '%s' must contain a valid ItemColor".formatted(variableName));
    }
}
