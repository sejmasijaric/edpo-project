package org.unisg.ftengrave.factoryeventstreams.streams;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;

public class StageCommandStationTransformer implements Transformer<String, String, KeyValue<String, String>> {

  private final StageCommandStationMapper stationMapper;
  private ProcessorContext context;

  public StageCommandStationTransformer(StageCommandStationMapper stationMapper) {
    this.stationMapper = stationMapper;
  }

  @Override
  public void init(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public KeyValue<String, String> transform(String key, String commandJson) {
    if (commandJson == null) {
      return null;
    }
    return stationMapper.toAssignment(commandJson, context.timestamp()).orElse(null);
  }

  @Override
  public void close() {
  }
}
