package org.unisg.ftengrave.sorterintegrationservice.service;

import java.util.Map;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.sorterintegrationservice.dto.SortingMachineCommandDto;

@Service
public class SortingMachineService {

  private static final Map<String, String> COMMAND_TO_SINK = Map.of(
      "request-sort-to-reject", "sink_1",
      "request-sort-to-shipping", "sink_2",
      "request-sort-to-retry", "sink_3");

  private final SorterHttpService sorterHttpService;

  public SortingMachineService(SorterHttpService sorterHttpService) {
    this.sorterHttpService = sorterHttpService;
  }

  public void handle(SortingMachineCommandDto sortingMachineCommandDto) {
    String commandType = sortingMachineCommandDto.getCommandType();
    if ("request-color-detection".equals(commandType)) {
      sorterHttpService.detectColor();
      return;
    }

    String sinkIdentifier = COMMAND_TO_SINK.get(commandType);

    if (sinkIdentifier == null) {
      throw new IllegalArgumentException("Unsupported sorting command type: " + commandType);
    }

    sorterHttpService.sortToSink(sinkIdentifier);
  }
}
