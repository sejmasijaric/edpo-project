package org.unisg.ftengrave.orderorchestrator;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.unisg.ftengrave.orderorchestrator.port.in.StartOrderOrchestrationUseCase;

@RestController
@RequiredArgsConstructor
public class TemporaryStartOrderOrchestrationController {

    private final StartOrderOrchestrationUseCase startOrderOrchestrationUseCase;

    @PostMapping("/temporary/start-order-orchestration/{orderIdentifier}")
    public ResponseEntity<Void> startOrderOrchestration(@PathVariable String orderIdentifier) {
        try {
            return startOrderOrchestrationUseCase.startOrderOrchestration(orderIdentifier)
                    ? ResponseEntity.accepted().build()
                    : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DuplicateBusinessKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
