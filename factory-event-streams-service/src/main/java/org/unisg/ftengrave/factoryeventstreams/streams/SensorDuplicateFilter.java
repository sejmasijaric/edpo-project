package org.unisg.ftengrave.factoryeventstreams.streams;

import java.util.Objects;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.factoryeventstreams.dto.SensorLevelEvent;

@Component
public class SensorDuplicateFilter {

  public boolean isDuplicate(String previousSensorValue, SensorLevelEvent currentEvent) {
    return currentEvent != null && Objects.equals(previousSensorValue, currentEvent.getSensorValue());
  }
}
