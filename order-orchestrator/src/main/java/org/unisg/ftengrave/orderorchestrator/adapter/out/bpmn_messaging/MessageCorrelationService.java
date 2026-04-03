package org.unisg.ftengrave.orderorchestrator.adapter.out.bpmn_messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.dto.message.MessageCorrelationResultDto;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.orderorchestrator.DuplicateBusinessKeyException;
import org.unisg.ftengrave.orderorchestrator.config.CamundaBusinessKeyConstraintInitializer;
import org.unisg.ftengrave.orderorchestrator.port.out.CorrelateMessagePort;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageCorrelationService implements CorrelateMessagePort {

    private final RuntimeService runtimeService;
    private final ObjectMapper objectMapper;

    @Override
    public MessageCorrelationResult correlateMessage(String messageName, String orderIdentifier, Map<String, Object> variables) {
        validateOrderIdentifier(orderIdentifier);
        Map<String, Object> sanitizedVariables = sanitizeVariables(variables);
        try {
            log.info("Consuming message {}", messageName);

            MessageCorrelationBuilder messageCorrelationBuilder = runtimeService.createMessageCorrelation(messageName);

            if (!sanitizedVariables.isEmpty()) {
                messageCorrelationBuilder.setVariables(sanitizedVariables);
            }

            MessageCorrelationResult messageResult = messageCorrelationBuilder.processInstanceBusinessKey(orderIdentifier)
                    .correlateWithResult();

            String messageResultJson =
                    objectMapper.writeValueAsString(MessageCorrelationResultDto.fromMessageCorrelationResult(messageResult));

            log.info("Correlation successful. Process Instance Id: {}", messageResultJson);
            log.info("Correlation key used: {}", orderIdentifier);

            return messageResult;
        } catch (MismatchingMessageCorrelationException e) {
            log.error("Issue when correlating the message: {}", e.getMessage());
        } catch (Exception e) {
            if (isDuplicateBusinessKeyViolation(e)) {
                throw new DuplicateBusinessKeyException(orderIdentifier, e);
            }
            log.error("Unknown issue occurred", e);
        }
        return null;
    }

    private boolean isDuplicateBusinessKeyViolation(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            String message = current.getMessage();
            if (message != null && message.contains(CamundaBusinessKeyConstraintInitializer.BUSINESS_KEY_INDEX_NAME)) {
                return true;
            }
            current = current.getCause();
        }
        return false;
    }

    private Map<String, Object> sanitizeVariables(Map<String, Object> rawVariables) {
        if (rawVariables == null || rawVariables.isEmpty()) {
            return Map.of();
        }

        List<String> omittedPaths = new ArrayList<>();
        Map<String, Object> sanitizedVariables = sanitizeMap(rawVariables, null, omittedPaths);
        if (!omittedPaths.isEmpty()) {
            log.warn("Omitting null-valued process variables before Camunda correlation: {}", omittedPaths);
        }
        return sanitizedVariables;
    }

    private Map<String, Object> sanitizeMap(Map<String, Object> source, String path, List<String> omittedPaths) {
        Map<String, Object> sanitized = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String currentPath = path == null ? entry.getKey() : path + "." + entry.getKey();
            Object sanitizedValue = sanitizeValue(entry.getValue(), currentPath, omittedPaths);
            if (sanitizedValue != null) {
                sanitized.put(entry.getKey(), sanitizedValue);
            }
        }
        return sanitized;
    }

    private Object sanitizeValue(Object value, String path, List<String> omittedPaths) {
        if (value == null) {
            omittedPaths.add(path);
            return null;
        }

        if (value instanceof Map<?, ?> nestedMap) {
            return sanitizeNestedMap(nestedMap, path, omittedPaths);
        }

        if (value instanceof Enum<?> enumValue) {
            return enumValue.name();
        }

        return value;
    }

    private Map<String, Object> sanitizeNestedMap(Map<?, ?> nestedMap, String path, List<String> omittedPaths) {
        Map<String, Object> convertedMap = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : nestedMap.entrySet()) {
            if (!(entry.getKey() instanceof String key)) {
                throw new IllegalArgumentException("Process variable maps must use String keys");
            }
            convertedMap.put(key, entry.getValue());
        }
        return sanitizeMap(convertedMap, path, omittedPaths);
    }

    private void validateOrderIdentifier(String orderIdentifier) {
        if (orderIdentifier == null || orderIdentifier.isBlank()) {
            throw new IllegalArgumentException("Message payload must contain a non-blank orderIdentifier");
        }
    }
}
