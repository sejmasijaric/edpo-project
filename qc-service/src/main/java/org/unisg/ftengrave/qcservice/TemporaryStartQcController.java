package org.unisg.ftengrave.qcservice;

import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.runtime.MessageCorrelationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.MessageCorrelationService;
import org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.dto.CamundaMessageDto;
import org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.dto.MessageProcessDto;

@RestController
@RequiredArgsConstructor
public class TemporaryStartQcController {

    private static final String START_QC_MESSAGE = "StartQcMessage";

    private final MessageCorrelationService messageCorrelationService;

    @PostMapping("/temporary/start-qc/{itemIdentifier}")
    public ResponseEntity<Void> startQc(@PathVariable String itemIdentifier) {
        try {
            CamundaMessageDto message = CamundaMessageDto.builder()
                    .dto(MessageProcessDto.builder()
                            .itemIdentifier(itemIdentifier)
                            .build())
                    .build();

            MessageCorrelationResult result = messageCorrelationService.correlateMessage(message, START_QC_MESSAGE);
            return result != null
                    ? ResponseEntity.accepted().build()
                    : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DuplicateBusinessKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
