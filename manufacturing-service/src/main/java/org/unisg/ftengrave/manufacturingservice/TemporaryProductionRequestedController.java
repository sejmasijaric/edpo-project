package org.unisg.ftengrave.manufacturingservice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;
import org.unisg.ftengrave.manufacturingservice.port.in.StartManufacturingUseCase;

@RestController
@RequiredArgsConstructor
public class TemporaryProductionRequestedController {

    private final StartManufacturingUseCase startManufacturingUseCase;

    @PostMapping("/temporary/production-requested/{itemIdentifier}")
    public ResponseEntity<Void> productionRequested(@PathVariable String itemIdentifier) {
        try {
            return startManufacturingUseCase.startManufacturing(itemIdentifier)
                    ? ResponseEntity.accepted().build()
                    : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DuplicateBusinessKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
