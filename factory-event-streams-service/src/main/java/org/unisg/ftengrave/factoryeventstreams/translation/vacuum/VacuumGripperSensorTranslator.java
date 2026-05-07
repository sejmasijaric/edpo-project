package org.unisg.ftengrave.factoryeventstreams.translation.vacuum;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.factoryeventstreams.dto.SensorLevelEvent;
import org.unisg.ftengrave.factoryeventstreams.dto.TranslatedMachineEvent;
import org.unisg.ftengrave.factoryeventstreams.translation.AbstractStationSensorTranslator;

@Component
public class VacuumGripperSensorTranslator extends AbstractStationSensorTranslator {

  public VacuumGripperSensorTranslator(
      ObjectMapper objectMapper,
      @Value("${factory-streams.vacuum-gripper.station-identifiers:FTFactory/VGR_1,VGR_1,vgr_1,vacuum-gripper}")
      String stationIdentifiers,
      @Value("${kafka.topic.vacuum-gripper-event}") String outputTopic) {
    super(objectMapper, stationIdentifiers, outputTopic);
  }

  @Override
  public List<TranslatedMachineEvent> translate(SensorLevelEvent event) {
    return mapLightBarrierEvent(
            event, "i7_light_barrier", "item-arrived-at-intake", "item-left-intake")
        .or(() -> mapLightBarrierEvent(
            event, "i4_light_barrier", "item-arrived-at-output", "item-left-output"))
        .map(List::of)
        .orElseGet(List::of);
  }
}
