package org.unisg.ftengrave.factoryeventstreams.translation.engraver;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.factoryeventstreams.dto.SensorLevelEvent;
import org.unisg.ftengrave.factoryeventstreams.dto.TranslatedMachineEvent;
import org.unisg.ftengrave.factoryeventstreams.translation.AbstractStationSensorTranslator;

@Component
public class EngraverSensorTranslator extends AbstractStationSensorTranslator {

  public EngraverSensorTranslator(
      ObjectMapper objectMapper,
      @Value("${factory-streams.engraver.station-identifiers:FTFactory/OV_1,OV_1,oven,engraver}")
      String stationIdentifiers,
      @Value("${kafka.topic.engraver-event}") String outputTopic) {
    super(objectMapper, stationIdentifiers, outputTopic);
  }

  @Override
  public List<TranslatedMachineEvent> translate(SensorLevelEvent event) {
    return mapLightBarrierEvent(
            event,
            "i5_light_barrier",
            "item-arrived-at-engraver-sink",
            "item-left-engraver-sink")
        .map(List::of)
        .orElseGet(List::of);
  }
}
