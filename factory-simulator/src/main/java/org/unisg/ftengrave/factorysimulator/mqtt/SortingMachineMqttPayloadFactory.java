package org.unisg.ftengrave.factorysimulator.mqtt;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import org.unisg.ftengrave.factorysimulator.domain.Item;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulatorService;
import org.springframework.stereotype.Component;

@Component
public class SortingMachineMqttPayloadFactory {

  private final ObjectMapper objectMapper;
  private final FactorySimulatorService factorySimulatorService;

  public SortingMachineMqttPayloadFactory(
      ObjectMapper objectMapper,
      FactorySimulatorService factorySimulatorService) {
    this.objectMapper = objectMapper;
    this.factorySimulatorService = factorySimulatorService;
  }

  public String createPayload() {
    Map<String, Object> payload = new LinkedHashMap<>();
    Item sink_i_item = factorySimulatorService.getSink("SM-I").item();
    Item sink_s1_item = factorySimulatorService.getSink("SINK-S1").item();
    Item sink_s2_item = factorySimulatorService.getSink("SINK-S2").item();
    Item sink_s3_item = factorySimulatorService.getSink("SINK-S3").item();
    Item sorter_start_item = factorySimulatorService.getSink("MM-ejection").item();

    payload.put("timestamp", Instant.now().toString());
    payload.put("i1_light_barrier", sorter_start_item==null ? 1 : 0);
    payload.put("i2_color_sensor", mapColorSensor(sink_i_item));
    payload.put("i3_light_barrier", sink_i_item == null ? 1 : 0);
    payload.put("i6_light_barrier", sink_s1_item == null ? 1 : 0);
    payload.put("i7_light_barrier", sink_s2_item == null ? 1 : 0);
    payload.put("i8_light_barrier", sink_s3_item == null ? 1 : 0);
    payload.put("current_task", "PLACEHOLDER");
    payload.put("current_task_duration", 2.0d);

    try {
      return objectMapper.writeValueAsString(payload);
    } catch (JsonProcessingException exception) {
      throw new IllegalStateException("Failed to serialize sorter MQTT payload", exception);
    }
  }

  private int mapColorSensor(Item sorterInputItem) {
    if (sorterInputItem == null) {
      return 0;
    }

    return switch (sorterInputItem.color()) {
      case White -> 1000;
      case Red -> 1300;
      case Blue -> 1700;
    };
  }
}
