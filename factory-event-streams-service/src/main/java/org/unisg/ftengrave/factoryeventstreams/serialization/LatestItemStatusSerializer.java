package org.unisg.ftengrave.factoryeventstreams.serialization;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serializer;
import org.unisg.ftengrave.factoryeventstreams.dto.LatestItemStatus;

public class LatestItemStatusSerializer implements Serializer<LatestItemStatus> {

  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  public byte[] serialize(String topic, LatestItemStatus data) {
    if (data == null) {
      return null;
    }
    try {
      return objectMapper.writeValueAsBytes(data);
    } catch (Exception exception) {
      throw new IllegalArgumentException("Could not serialize latest item status", exception);
    }
  }
}
