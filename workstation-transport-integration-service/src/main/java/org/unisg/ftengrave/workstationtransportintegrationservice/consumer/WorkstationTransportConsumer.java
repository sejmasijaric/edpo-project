package org.unisg.ftengrave.workstationtransportintegrationservice.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.workstationtransportintegrationservice.dto.WorkstationTransportCommandDto;
import org.unisg.ftengrave.workstationtransportintegrationservice.service.WorkstationTransportService;

@Component
public class WorkstationTransportConsumer {

  private final WorkstationTransportService workstationTransportService;

  public WorkstationTransportConsumer(WorkstationTransportService workstationTransportService) {
    this.workstationTransportService = workstationTransportService;
  }

  @KafkaListener(topics = "${kafka.topic.workstation-transport-command}")
  public void consume(WorkstationTransportCommandDto workstationTransportCommandDto) {
    workstationTransportService.handle(workstationTransportCommandDto);
  }
}
