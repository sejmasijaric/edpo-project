package org.unisg.ftengrave.factoryeventstreams.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import org.apache.kafka.common.serialization.Deserializer;
import org.unisg.ftengrave.factoryeventstreams.dto.LatestItemStatus;

public class LatestItemStatusDeserializer implements Deserializer<LatestItemStatus> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public LatestItemStatus deserialize(String topic, byte[] data) {
    if (data == null) {
      return null;
    }
    try {
      return objectMapper.readValue(new String(data, StandardCharsets.UTF_8), LatestItemStatus.class);
    } catch (Exception exception) {
      throw new IllegalArgumentException("Could not deserialize latest item status", exception);
    }
  }
}
