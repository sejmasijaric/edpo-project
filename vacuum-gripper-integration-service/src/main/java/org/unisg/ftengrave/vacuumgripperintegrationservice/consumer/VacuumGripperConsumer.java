package org.unisg.ftengrave.vacuumgripperintegrationservice.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.vacuumgripperintegrationservice.dto.VacuumGripperCommandDto;
import org.unisg.ftengrave.vacuumgripperintegrationservice.service.VacuumGripperService;

@Component
public class VacuumGripperConsumer {

  private final VacuumGripperService vacuumGripperService;

  public VacuumGripperConsumer(VacuumGripperService vacuumGripperService) {
    this.vacuumGripperService = vacuumGripperService;
  }

  @KafkaListener(topics = "${kafka.topic.vacuum-gripper-command}")
  public void consume(VacuumGripperCommandDto vacuumGripperCommandDto) {
    vacuumGripperService.handle(vacuumGripperCommandDto);
  }
}
