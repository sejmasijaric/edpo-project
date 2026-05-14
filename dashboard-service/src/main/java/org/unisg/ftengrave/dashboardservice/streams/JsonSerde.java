package org.unisg.ftengrave.dashboardservice.streams;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.apache.kafka.common.errors.SerializationException;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class JsonSerde<T> implements Serde<T> {

  private final Serializer<T> serializer;
  private final Deserializer<T> deserializer;

  public JsonSerde(ObjectMapper objectMapper, Class<T> type) {
    this.serializer = new JsonSerializer<>(objectMapper);
    this.deserializer = new JsonDeserializer<>(objectMapper, type);
  }

  @Override
  public Serializer<T> serializer() {
    return serializer;
  }

  @Override
  public Deserializer<T> deserializer() {
    return deserializer;
  }

  private static class JsonSerializer<T> implements Serializer<T> {

    private final ObjectMapper objectMapper;

    JsonSerializer(ObjectMapper objectMapper) {
      this.objectMapper = objectMapper;
    }

    @Override
    public byte[] serialize(String topic, T data) {
      if (data == null) {
        return null;
      }
      try {
        return objectMapper.writeValueAsBytes(data);
      } catch (Exception exception) {
        throw new SerializationException("Could not serialize JSON value", exception);
      }
    }
  }

  private static class JsonDeserializer<T> implements Deserializer<T> {

    private final ObjectMapper objectMapper;
    private final Class<T> type;

    JsonDeserializer(ObjectMapper objectMapper, Class<T> type) {
      this.objectMapper = objectMapper;
      this.type = type;
    }

    @Override
    public T deserialize(String topic, byte[] data) {
      if (data == null || data.length == 0) {
        return null;
      }
      try {
        return objectMapper.readValue(data, type);
      } catch (Exception exception) {
        throw new SerializationException("Could not deserialize JSON value", exception);
      }
    }
  }

  @Override
  public void configure(Map<String, ?> configs, boolean isKey) {
  }

  @Override
  public void close() {
  }
}
