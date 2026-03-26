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
import org.unisg.ftengrave.qcservice.adapter.out.bpmn_messaging.dto.ColorDetectedMessageProcessDto;
import org.unisg.ftengrave.qcservice.domain.ItemColor;

@RestController
@RequiredArgsConstructor
public class TemporaryColorDetectedController {

    private static final String COLOR_DETECTED_MESSAGE = "ColorDetectedMessage";

    private final MessageCorrelationService messageCorrelationService;

    @PostMapping("/temporary/color-detected/{itemIdentifier}/{color}")
    public ResponseEntity<Void> colorDetected(@PathVariable String itemIdentifier, @PathVariable ItemColor color) {
        CamundaMessageDto message = CamundaMessageDto.builder()
                .dto(ColorDetectedMessageProcessDto.builder()
                        .itemIdentifier(itemIdentifier)
                        .color(color)
                        .build())
                .build();

        MessageCorrelationResult result = messageCorrelationService.correlateMessage(message, COLOR_DETECTED_MESSAGE);
        return result != null
                ? ResponseEntity.accepted().build()
                : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }
}
