package org.unisg.ftengrave.qcservice.adapter.in.delegate;

import org.camunda.bpm.engine.delegate.BpmnError;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.application.MatchItemColorService;
import org.unisg.ftengrave.qcservice.domain.ItemColor;

@Component("MatchItemColorAdapter")
public class MatchItemColorAdapter implements JavaDelegate {

    static final String DETECTED_COLOR_VARIABLE = "detected-color";
    static final String TARGET_COLOR_VARIABLE = "targetColor";
    static final String PASSED_COLOR_CHECK_VARIABLE = "passedColorCheck";
    static final String COLOR_DETECTION_FAILED_ERROR = "COLOR_DETECTION_FAILED";

    private final MatchItemColorService matchItemColorService;

    public MatchItemColorAdapter(MatchItemColorService matchItemColorService) {
        this.matchItemColorService = matchItemColorService;
    }

    @Override
    public void execute(DelegateExecution delegateExecution) {
        ItemColor detectedColor = getRequiredColor(delegateExecution, DETECTED_COLOR_VARIABLE);
        if (detectedColor == ItemColor.NONE) {
            throw new BpmnError(COLOR_DETECTION_FAILED_ERROR, "No color detected");
        }

        ItemColor targetColor = getRequiredColor(delegateExecution, TARGET_COLOR_VARIABLE);
        boolean passedColorCheck = matchItemColorService.matches(detectedColor, targetColor);
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
