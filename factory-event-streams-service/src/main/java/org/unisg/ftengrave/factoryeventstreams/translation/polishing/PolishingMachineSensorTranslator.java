package org.unisg.ftengrave.factoryeventstreams.translation.polishing;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.factoryeventstreams.dto.SensorLevelEvent;
import org.unisg.ftengrave.factoryeventstreams.dto.TranslatedMachineEvent;
import org.unisg.ftengrave.factoryeventstreams.translation.AbstractStationSensorTranslator;

@Component
public class PolishingMachineSensorTranslator extends AbstractStationSensorTranslator {

  public PolishingMachineSensorTranslator(
      ObjectMapper objectMapper,
      @Value("${factory-streams.polishing-machine.station-identifiers:FTFactory/MM_1,MM_1,mm_1,polishing-machine}")
      String stationIdentifiers,
      @Value("${kafka.topic.polishing-machine-event}") String outputTopic) {
    super(objectMapper, stationIdentifiers, outputTopic);
  }

  @Override
  public List<TranslatedMachineEvent> translate(SensorLevelEvent event) {
    return mapLightBarrierEvent(
            event,
            "i4_light_barrier",
            "item-arrived-at-polishing-machine-output",
            "item-left-polishing-machine-output")
        .map(List::of)
        .orElseGet(List::of);
  }
}
