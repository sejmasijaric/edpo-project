package org.unisg.ftengrave.factoryeventstreams.streams;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.unisg.ftengrave.factoryeventstreams.dto.SensorLevelEvent;

public class SensorDuplicateFilterTransformer
    implements Transformer<String, SensorLevelEvent, KeyValue<String, SensorLevelEvent>> {

  public static final String STORE_NAME = "factory-sensor-last-value-store";

  private static final Logger LOGGER =
      LoggerFactory.getLogger(SensorDuplicateFilterTransformer.class);

  private final SensorDuplicateFilter duplicateFilter;
  private KeyValueStore<String, String> lastSensorValues;

  public SensorDuplicateFilterTransformer(SensorDuplicateFilter duplicateFilter) {
    this.duplicateFilter = duplicateFilter;
  }

  @Override
  public void init(ProcessorContext context) {
    this.lastSensorValues = context.getStateStore(STORE_NAME);
  }

  @Override
  public KeyValue<String, SensorLevelEvent> transform(String key, SensorLevelEvent event) {
    String sensorKey = event.sensorKey();
    String previousValue = lastSensorValues.get(sensorKey);
    if (duplicateFilter.isDuplicate(previousValue, event)) {
      LOGGER.debug("Filtered duplicate sensor event for {}", sensorKey);
      return null;
    }

    lastSensorValues.put(sensorKey, event.getSensorValue());
    LOGGER.debug("Forwarding sensor event {}={}", sensorKey, event.getSensorValue());
    return KeyValue.pair(key, event);
  }

  @Override
  public void close() {
  }
}
