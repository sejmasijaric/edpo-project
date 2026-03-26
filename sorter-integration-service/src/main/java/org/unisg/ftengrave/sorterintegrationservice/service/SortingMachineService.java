package org.unisg.ftengrave.sorterintegrationservice.service;

import java.util.Map;
import org.springframework.stereotype.Service;
import org.unisg.ftengrave.sorterintegrationservice.dto.SortingMachineEventDto;

@Service
public class SortingMachineService {

  private static final Map<String, String> EVENT_TO_SINK = Map.of(
      "sort-to-reject", "sink_1",
      "sort-to-shipping", "sink_2",
      "sort-to-retry", "sink_3");

  private final SorterHttpService sorterHttpService;

  public SortingMachineService(SorterHttpService sorterHttpService) {
    this.sorterHttpService = sorterHttpService;
  }

  public void handle(SortingMachineEventDto sortingMachineEventDto) {
    String eventType = sortingMachineEventDto.getEventType();
    String sinkIdentifier = EVENT_TO_SINK.get(eventType);

    if (sinkIdentifier == null) {
      throw new IllegalArgumentException("Unsupported sorting event type: " + eventType);
    }

    sorterHttpService.sortToSink(sinkIdentifier);
  }
}
