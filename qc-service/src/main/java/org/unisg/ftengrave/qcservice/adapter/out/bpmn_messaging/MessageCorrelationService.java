package org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.MismatchingMessageCorrelationException;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.rest.dto.message.MessageCorrelationResultDto;
import org.camunda.bpm.engine.runtime.MessageCorrelationBuilder;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.qcservice.DuplicateBusinessKeyException;
import org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.dto.CamundaMessageDto;
import org.unisg.ftengrave.qcservice.config.CamundaBusinessKeyConstraintInitializer;

import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class MessageCorrelationService {

    private final RuntimeService runtimeService;
    private final ObjectMapper objectMapper;

    public MessageCorrelationResult correlateMessage(CamundaMessageDto camundaMessageDto, String messageName) {
        Map<String, Object> variables = extractVariables(camundaMessageDto);
        String itemIdentifier = extractItemIdentifier(variables);

        try {
            log.info("Consuming message {}", messageName);

            MessageCorrelationBuilder messageCorrelationBuilder = runtimeService.createMessageCorrelation(messageName);

            if (!variables.isEmpty()) {
                messageCorrelationBuilder.setVariables(variables);
            }

            MessageCorrelationResult messageResult = messageCorrelationBuilder.processInstanceBusinessKey(itemIdentifier)
                    .correlateWithResult();

            String messageResultJson = objectMapper.writeValueAsString(MessageCorrelationResultDto.fromMessageCorrelationResult(messageResult));

            log.info("Correlation successful. Process Instance Id: {}", messageResultJson);
            log.info("Correlation key used: {}", itemIdentifier);

            return messageResult;
        } catch (MismatchingMessageCorrelationException e) {
            log.error("Issue when correlating the message: {}", e.getMessage());
        } catch (Exception e) {
            if (isDuplicateBusinessKeyViolation(e)) {
                throw new DuplicateBusinessKeyException(itemIdentifier, e);
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

    private Map<String, Object> extractVariables(CamundaMessageDto camundaMessageDto) {
        if (camundaMessageDto == null || camundaMessageDto.getDto() == null) {
            return Map.of();
        }

        return objectMapper.convertValue(camundaMessageDto.getDto(), objectMapper.getTypeFactory()
                .constructMapType(Map.class, String.class, Object.class));
    }

    private String extractItemIdentifier(Map<String, Object> variables) {
        Object itemIdentifier = variables.get("itemIdentifier");
        if (!(itemIdentifier instanceof String identifier) || identifier.isBlank()) {
            throw new IllegalArgumentException("Message payload must contain a non-blank itemIdentifier");
        }
        return identifier;
    }
}
