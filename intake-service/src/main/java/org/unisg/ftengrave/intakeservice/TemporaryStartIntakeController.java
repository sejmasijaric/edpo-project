package org.unisg.ftengrave.intakeservice;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.unisg.ftengrave.intakeservice.domain.ItemColor;
import org.unisg.ftengrave.intakeservice.port.in.StartIntakeUseCase;

@RestController
@RequiredArgsConstructor
public class TemporaryStartIntakeController {

    private final StartIntakeUseCase startIntakeUseCase;

    @PostMapping("/temporary/start-intake/{itemIdentifier}")
    public ResponseEntity<Void> orderCreated(@PathVariable String itemIdentifier, @RequestParam ItemColor targetColor) {
        try {
            return startIntakeUseCase.startIntake(itemIdentifier, targetColor)
                    ? ResponseEntity.accepted().build()
                    : ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (DuplicateBusinessKeyException exception) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }
}
