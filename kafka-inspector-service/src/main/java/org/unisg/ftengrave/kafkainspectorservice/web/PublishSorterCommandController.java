package org.unisg.ftengrave.kafkainspectorservice.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.unisg.ftengrave.kafkainspectorservice.publisher.PublishSorterCommandUseCase;

@RestController
@RequestMapping("/api/kafka/commands")
public class PublishSorterCommandController {

  private final PublishSorterCommandUseCase publishSorterCommandUseCase;

  public PublishSorterCommandController(PublishSorterCommandUseCase publishSorterCommandUseCase) {
    this.publishSorterCommandUseCase = publishSorterCommandUseCase;
  }

  @PostMapping("/{commandName}")
  public ResponseEntity<Void> publish(@PathVariable String commandName) {
    publishSorterCommandUseCase.publish(commandName);
    return ResponseEntity.accepted().build();
  }
}
