package org.unisg.ftengrave.qcservice.port.out;

import org.camunda.bpm.engine.runtime.MessageCorrelationResult;

import java.util.Map;

public interface CorrelateMessagePort {

    MessageCorrelationResult correlateMessage(String messageName, String itemIdentifier, Map<String, Object> variables);
}
