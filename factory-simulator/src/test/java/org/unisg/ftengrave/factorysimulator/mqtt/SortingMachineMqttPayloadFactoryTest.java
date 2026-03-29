package org.unisg.ftengrave.factorysimulator.mqtt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factorysimulator.domain.ItemColor;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulatorService;

class SortingMachineMqttPayloadFactoryTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final FactorySimulatorService factorySimulatorService = new FactorySimulatorService();
  private final SortingMachineMqttPayloadFactory payloadFactory =
      new SortingMachineMqttPayloadFactory(objectMapper, factorySimulatorService);

  @Test
  void reportsClosedI3WhenSorterInputContainsAnItem() throws Exception {
    factorySimulatorService.addItem("ITEM-1001", ItemColor.Red, "SM-I");

    JsonNode payload = objectMapper.readTree(payloadFactory.createPayload());

    assertEquals(0, payload.get("i3_light_barrier").asInt());
    assertEquals(1300, payload.get("i2_color_sensor").asInt());
    assertEquals("detect_color", payload.get("current_task").asText());
  }

  @Test
  void reportsOpenI3WhenSorterInputIsEmpty() throws Exception {
    JsonNode payload = objectMapper.readTree(payloadFactory.createPayload());

    assertEquals(1, payload.get("i3_light_barrier").asInt());
    assertEquals(0, payload.get("i2_color_sensor").asInt());
    assertEquals("idle", payload.get("current_task").asText());
  }
}
