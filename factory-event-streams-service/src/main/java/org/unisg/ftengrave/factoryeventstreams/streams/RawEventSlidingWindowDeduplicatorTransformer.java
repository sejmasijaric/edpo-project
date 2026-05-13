package org.unisg.ftengrave.factoryeventstreams.streams;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.HexFormat;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.kstream.Transformer;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.WindowStore;
import org.apache.kafka.streams.state.WindowStoreIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RawEventSlidingWindowDeduplicatorTransformer
    implements Transformer<String, String, KeyValue<String, String>> {

  public static final String STORE_NAME = "factory-raw-event-dedup-window-store";
  public static final Duration WINDOW = Duration.ofSeconds(1);

  private static final Logger LOGGER =
      LoggerFactory.getLogger(RawEventSlidingWindowDeduplicatorTransformer.class);

  private final Duration window;
  private WindowStore<String, Long> seenRawEvents;
  private ProcessorContext context;

  public RawEventSlidingWindowDeduplicatorTransformer() {
    this(WINDOW);
  }

  RawEventSlidingWindowDeduplicatorTransformer(Duration window) {
    this.window = window;
  }

  @Override
  public void init(ProcessorContext context) {
    this.context = context;
    this.seenRawEvents = context.getStateStore(STORE_NAME);
  }

  @Override
  public KeyValue<String, String> transform(String sourceTopic, String rawPayload) {
    long eventTimestamp = context.timestamp();
    String fingerprint = fingerprint(sourceTopic, rawPayload);
    long windowStart = Math.max(0L, eventTimestamp - window.toMillis());

    try (WindowStoreIterator<Long> previousEvents =
        seenRawEvents.fetch(fingerprint, windowStart, eventTimestamp)) {
      if (previousEvents.hasNext()) {
        LOGGER.debug("Filtered duplicate raw factory event from MQTT topic {}", sourceTopic);
        return null;
      }
    }

    seenRawEvents.put(fingerprint, eventTimestamp, eventTimestamp);
    return KeyValue.pair(sourceTopic, rawPayload);
  }

  @Override
  public void close() {
  }

  private String fingerprint(String sourceTopic, String rawPayload) {
    try {
      MessageDigest digest = MessageDigest.getInstance("SHA-256");
      digest.update(nullToEmpty(sourceTopic).getBytes(StandardCharsets.UTF_8));
      digest.update((byte) 0);
      digest.update(nullToEmpty(rawPayload).getBytes(StandardCharsets.UTF_8));
      return HexFormat.of().formatHex(digest.digest());
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 digest is not available", exception);
    }
  }

  private String nullToEmpty(String value) {
    return value == null ? "" : value;
  }
}
