package org.unisg.ftengrave.engraverintegrationservice.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.engraverintegrationservice.dto.EngraverCommandDto;
import org.unisg.ftengrave.engraverintegrationservice.service.EngraverService;

@Component
public class EngraverConsumer {

  private final EngraverService engraverService;

  public EngraverConsumer(EngraverService engraverService) {
    this.engraverService = engraverService;
  }

  @KafkaListener(topics = "${kafka.topic.engraver-command}")
  public void consume(EngraverCommandDto engraverCommandDto) {
    engraverService.handle(engraverCommandDto);
  }
}
