package org.unisg.ftengrave.polishingmachineintegrationservice.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.polishingmachineintegrationservice.dto.PolishingMachineCommandDto;
import org.unisg.ftengrave.polishingmachineintegrationservice.service.PolishingMachineService;

@Component
public class PolishingMachineConsumer {

  private final PolishingMachineService polishingMachineService;

  public PolishingMachineConsumer(PolishingMachineService polishingMachineService) {
    this.polishingMachineService = polishingMachineService;
  }

  @KafkaListener(topics = "${kafka.topic.polishing-machine-command}")
  public void consume(PolishingMachineCommandDto polishingMachineCommandDto) {
    polishingMachineService.handle(polishingMachineCommandDto);
  }
}
