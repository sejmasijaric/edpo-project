package org.unisg.ftengrave.factoryeventstreams.translation.workstation;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.factoryeventstreams.dto.SensorLevelEvent;
import org.unisg.ftengrave.factoryeventstreams.dto.TranslatedMachineEvent;
import org.unisg.ftengrave.factoryeventstreams.translation.AbstractStationSensorTranslator;

@Component
public class WorkstationTransportSensorTranslator extends AbstractStationSensorTranslator {

  private static final String READY_STATE = "ready";
  private static final String MOVE_COMPLETED_EVENT = "wt-move-completed";

  private final ConcurrentMap<String, Boolean> completedByStation = new ConcurrentHashMap<>();

  public WorkstationTransportSensorTranslator(
      ObjectMapper objectMapper,
      @Value("${factory-streams.workstation-transport.station-identifiers:FTFactory/WT_1,WT_1,wt_1,workstation-transport}")
      String stationIdentifiers,
      @Value("${kafka.topic.workstation-transport-event}") String outputTopic) {
    super(objectMapper, stationIdentifiers, outputTopic);
  }

  @Override
  public List<TranslatedMachineEvent> translate(SensorLevelEvent event) {
    if (!"current_state".equals(event.getSensorName())) {
      return List.of();
    }

    boolean completedNow =
        READY_STATE.equals(event.getSensorValue())
            && event.getMetadata().getOrDefault("currentTask", "").isEmpty();
    boolean completedBefore = completedByStation.getOrDefault(event.getStation(), false);
    completedByStation.put(event.getStation(), completedNow);

    if (completedNow && !completedBefore) {
      return List.of(toTranslatedMachineEvent(MOVE_COMPLETED_EVENT, event));
    }
    return List.of();
  }
}
