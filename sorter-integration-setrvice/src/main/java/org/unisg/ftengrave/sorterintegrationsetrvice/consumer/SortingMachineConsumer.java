package org.unisg.ftengrave.sorterintegrationsetrvice.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.sorterintegrationsetrvice.dto.SortingMachineEventDto;
import org.unisg.ftengrave.sorterintegrationsetrvice.service.SortingMachineService;

@Component
public class SortingMachineConsumer {

  private final SortingMachineService sortingMachineService;

  public SortingMachineConsumer(SortingMachineService sortingMachineService) {
    this.sortingMachineService = sortingMachineService;
  }

  @KafkaListener(topics = "${kafka.topic.sorting-machine}")
  public void consume(SortingMachineEventDto sortingMachineEventDto) {
    sortingMachineService.handle(sortingMachineEventDto);
  }
}
