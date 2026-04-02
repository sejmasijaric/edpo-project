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
        System.out.println("Check: " + rawValue + " " + variableName);
        System.out.println("execId = " + delegateExecution.getId());
        System.out.println("parentId = " + delegateExecution.getParentId());
        System.out.println("activityId = " + delegateExecution.getCurrentActivityId());
        System.out.println("processInstanceId = " + delegateExecution.getProcessInstanceId());

        System.out.println("targetColor visible = " + delegateExecution.getVariable("targetColor"));
        System.out.println("targetColor local   = " + delegateExecution.getVariableLocal("targetColor"));
        System.out.println("process targetColor = " + delegateExecution.getProcessInstance().getVariable("targetColor"));

        System.out.println("all local vars = " + delegateExecution.getVariablesLocal());
        System.out.println("all visible vars = " + delegateExecution.getVariables());

        if (rawValue instanceof ItemColor color) {
            return color;
        }
        if (rawValue instanceof String colorName) {
            ItemColor color = ItemColor.fromExternalValue(colorName);
            if (color != null) {
                return color;
            }
        }
        throw new IllegalStateException("Process variable '%s' must contain a valid ItemColor".formatted(variableName));
    }
}
