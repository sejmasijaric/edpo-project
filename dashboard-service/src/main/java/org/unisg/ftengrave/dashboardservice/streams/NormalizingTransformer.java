package org.unisg.ftengrave.dashboardservice.streams;

import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.unisg.ftengrave.dashboardservice.dto.DashboardEvent;

public class NormalizingTransformer implements Transformer<String, String, KeyValue<String, DashboardEvent>> {

  private final String sourceTopic;
  private final DashboardEventNormalizer normalizer;
  private ProcessorContext context;

  public NormalizingTransformer(String sourceTopic, DashboardEventNormalizer normalizer) {
    this.sourceTopic = sourceTopic;
    this.normalizer = normalizer;
  }

  @Override
  public void init(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public KeyValue<String, DashboardEvent> transform(String key, String value) {
    long timestamp = context == null ? System.currentTimeMillis() : context.timestamp();
    return normalizer.normalize(sourceTopic, key, value, timestamp)
        .map(event -> KeyValue.pair(event.eventId(), event))
        .orElse(null);
  }

  @Override
  public void close() {
  }
}
