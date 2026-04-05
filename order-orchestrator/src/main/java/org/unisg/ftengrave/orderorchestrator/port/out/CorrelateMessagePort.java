package org.unisg.ftengrave.orderorchestrator.port.out;

import org.camunda.bpm.engine.runtime.MessageCorrelationResult;

import java.util.Map;

public interface CorrelateMessagePort {

    MessageCorrelationResult correlateMessage(String messageName, String orderIdentifier, Map<String, Object> variables);
}
