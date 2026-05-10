package org.unisg.ftengrave.factoryeventstreams.streams;

import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.factoryeventstreams.dto.SensorLevelEvent;
import org.unisg.ftengrave.factoryeventstreams.dto.TranslatedMachineEvent;
import org.unisg.ftengrave.factoryeventstreams.translation.StationSensorTranslator;

@Component
public class StationEventRouter {

  private static final Logger LOGGER = LoggerFactory.getLogger(StationEventRouter.class);

  private final List<StationSensorTranslator> translators;

  public StationEventRouter(List<StationSensorTranslator> translators) {
    this.translators = translators;
  }

  public List<TranslatedMachineEvent> route(SensorLevelEvent event) {
    for (StationSensorTranslator translator : translators) {
      if (translator.supports(event)) {
        return translator.translate(event);
      }
    }
    LOGGER.debug("No station translator registered for station {} from topic {}",
        event.getStation(), event.getSourceTopic());
    return List.of();
  }
}
