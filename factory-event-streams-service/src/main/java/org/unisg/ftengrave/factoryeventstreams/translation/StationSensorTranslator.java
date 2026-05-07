package org.unisg.ftengrave.factoryeventstreams.translation;

import java.util.List;
import org.unisg.ftengrave.factoryeventstreams.dto.SensorLevelEvent;
import org.unisg.ftengrave.factoryeventstreams.dto.TranslatedMachineEvent;

public interface StationSensorTranslator {

  boolean supports(SensorLevelEvent event);

  List<TranslatedMachineEvent> translate(SensorLevelEvent event);
}
