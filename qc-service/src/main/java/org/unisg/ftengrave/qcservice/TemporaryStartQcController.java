package org.unisg.ftengrave.qcservice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.unisg.ftengrave.qcservice.domain.ItemColor;
import org.unisg.ftengrave.qcservice.port.in.StartQcUseCase;

@RestController
@RequiredArgsConstructor
public class TemporaryStartQcController {

    private final StartQcUseCase startQcUseCase;

    @PostMapping("/temporary/start-qc/{itemIdentifier}")
    public ResponseEntity<Void> startQc(@PathVariable String itemIdentifier, @RequestParam ItemColor targetColor) {
        try {
            return startQcUseCase.startQc(itemIdentifier, targetColor)
                    ? ResponseEntity.accepted().build()
                    : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DuplicateBusinessKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
