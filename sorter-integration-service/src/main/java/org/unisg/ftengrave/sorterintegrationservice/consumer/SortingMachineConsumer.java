package org.unisg.ftengrave.sorterintegrationservice.consumer;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.sorterintegrationservice.dto.SortingMachineCommandDto;
import org.unisg.ftengrave.sorterintegrationservice.service.SortingMachineService;

@Component
public class SortingMachineConsumer {

  private final SortingMachineService sortingMachineService;

  public SortingMachineConsumer(SortingMachineService sortingMachineService) {
    this.sortingMachineService = sortingMachineService;
  }

  @KafkaListener(topics = "${kafka.topic.sorting-machine}")
  public void consume(SortingMachineCommandDto sortingMachineCommandDto) {
    sortingMachineService.handle(sortingMachineCommandDto);
  }
}
