package org.unisg.ftengrave.factorysimulator.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.Map;
import org.unisg.ftengrave.factorysimulator.domain.Item;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulatorService;
import org.springframework.stereotype.Component;

@Component
public class SortingMachineMqttPayloadFactory {

  private static final int IDLE_COLOR_SENSOR_VALUE = 1800;

  private final ObjectMapper objectMapper;
  private final MqttTimestampFactory timestampFactory;
  private final FactorySimulatorService factorySimulatorService;

  public SortingMachineMqttPayloadFactory(
      ObjectMapper objectMapper,
      MqttTimestampFactory timestampFactory,
      FactorySimulatorService factorySimulatorService) {
    this.objectMapper = objectMapper;
    this.timestampFactory = timestampFactory;
    this.factorySimulatorService = factorySimulatorService;
  }

  public String createPayload() {
    Map<String, Object> payload = new LinkedHashMap<>();
    Item sink_i_item = factorySimulatorService.getSink("SM-I").item();
    Item sink_s1_item = factorySimulatorService.getSink("SINK-S1").item();
    Item sink_s2_item = factorySimulatorService.getSink("SINK-S2").item();
    Item sink_s3_item = factorySimulatorService.getSink("SINK-S3").item();
    Item sorter_start_item = factorySimulatorService.getSink("MM-ejection").item();

    payload.put("timestamp", timestampFactory.createTimestamp());
    payload.put("i1_light_barrier", sorter_start_item == null ? 1 : 0);
    payload.put("i2_color_sensor", IDLE_COLOR_SENSOR_VALUE);
    payload.put("i3_light_barrier", sink_i_item == null ? 1 : 0);
    payload.put("i6_light_barrier", sink_s1_item == null ? 1 : 0);
    payload.put("i7_light_barrier", sink_s2_item == null ? 1 : 0);
    payload.put("i8_light_barrier", sink_s3_item == null ? 1 : 0);
    payload.put("current_task", sink_i_item == null ? "idle" : "detect_color");
    payload.put("current_task_duration", sink_i_item == null ? 0.0d : 2.0d);

    return writePayload(payload);
  }

  public String createColorDetectionPayload(Item detectedItem) {
    Map<String, Object> payload = new LinkedHashMap<>();
    Item sink_s1_item = factorySimulatorService.getSink("SINK-S1").item();
    Item sink_s2_item = factorySimulatorService.getSink("SINK-S2").item();
    Item sink_s3_item = factorySimulatorService.getSink("SINK-S3").item();
    Item sorterStartItem = factorySimulatorService.getSink("MM-ejection").item();

    payload.put("timestamp", timestampFactory.createTimestamp());
    payload.put("i1_light_barrier", sorterStartItem == null ? 1 : 0);
    payload.put("i2_color_sensor", mapColorSensor(detectedItem));
    payload.put("i3_light_barrier", 1);
    payload.put("i6_light_barrier", sink_s1_item == null ? 1 : 0);
    payload.put("i7_light_barrier", sink_s2_item == null ? 1 : 0);
    payload.put("i8_light_barrier", sink_s3_item == null ? 1 : 0);
    payload.put("current_task", "detect_color");
    payload.put("current_task_duration", 2.0d);

    return writePayload(payload);
  }

  private String writePayload(Map<String, Object> payload) {
    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize sorter MQTT payload", exception);
    }
  }

  private int mapColorSensor(Item sorterInputItem) {
    if (sorterInputItem == null) {
      return IDLE_COLOR_SENSOR_VALUE;
    }

    return switch (sorterInputItem.color()) {
      case White -> 950;
      case Red -> 1350;
      case Blue -> 1650;
    };
  }
}
