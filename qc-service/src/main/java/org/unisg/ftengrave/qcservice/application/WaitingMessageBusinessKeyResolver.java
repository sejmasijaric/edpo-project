package org.unisg.ftengrave.qcservice.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.runtime.Execution;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class WaitingMessageBusinessKeyResolver {

    private final RuntimeService runtimeService;

    public String resolve(String messageName) {
        try {
            Execution execution = runtimeService.createExecutionQuery()
                    .messageEventSubscriptionName(messageName)
                    .singleResult();

            if (execution == null) {
                log.warn("Ignoring event because no process is waiting for {}", messageName);
                return null;
            }

            ProcessInstance processInstance = runtimeService.createProcessInstanceQuery()
                    .processInstanceId(execution.getProcessInstanceId())
                    .singleResult();

            if (processInstance == null || processInstance.getBusinessKey() == null || processInstance.getBusinessKey().isBlank()) {
                log.warn("Ignoring event because the waiting process instance has no business key for {}", messageName);
                return null;
            }

            return processInstance.getBusinessKey();
        } catch (Exception exception) {
            log.warn("Ignoring event because waiting process resolution is ambiguous for {}", messageName, exception);
            return null;
        }
    }
}
