package org.unisg.ftengrave.orderorchestrator;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;
import org.unisg.ftengrave.orderorchestrator.port.in.StartOrderOrchestrationUseCase;

@RestController
@RequiredArgsConstructor
public class TemporaryOrderCreatedController {

    private final StartOrderOrchestrationUseCase startOrderOrchestrationUseCase;

    @PostMapping("/temporary/order-created/{itemIdentifier}")
    public ResponseEntity<Void> orderCreated(@PathVariable String itemIdentifier, @RequestParam ItemColor targetColor) {
        try {
            return startOrderOrchestrationUseCase.startOrderOrchestration(itemIdentifier, targetColor)
                    ? ResponseEntity.accepted().build()
                    : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DuplicateBusinessKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
