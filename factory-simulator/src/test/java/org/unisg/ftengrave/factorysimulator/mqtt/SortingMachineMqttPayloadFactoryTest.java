package org.unisg.ftengrave.factorysimulator.mqtt;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factorysimulator.domain.ItemColor;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulatorService;

class SortingMachineMqttPayloadFactoryTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final FactorySimulatorService factorySimulatorService = new FactorySimulatorService();
  private final SortingMachineMqttPayloadFactory payloadFactory =
      new SortingMachineMqttPayloadFactory(
          objectMapper,
          new MqttTimestampFactory(),
          factorySimulatorService);

  @Test
  void reportsIdleColorSensorWhenSorterInputContainsAnItem() throws Exception {
    factorySimulatorService.addItem("ITEM-1001", ItemColor.Red, "SM-I");

    JsonNode payload = objectMapper.readTree(payloadFactory.createPayload());

    assertEquals(0, payload.get("i3_light_barrier").asInt());
    assertTrue(payload.get("i2_color_sensor").asInt() > 1700);
    assertEquals("detect_color", payload.get("current_task").asText());
    assertFullRawSorterEnvelope(payload);
  }

  @Test
  void reportsIdleColorSensorWhenSorterInputIsEmpty() throws Exception {
    JsonNode payload = objectMapper.readTree(payloadFactory.createPayload());

    assertEquals(1, payload.get("i3_light_barrier").asInt());
    assertTrue(payload.get("i2_color_sensor").asInt() > 1700);
    assertEquals("idle", payload.get("current_task").asText());
    assertFullRawSorterEnvelope(payload);
  }

  @Test
  void reportsDetectedColorWhenForcedColorDetectionPayloadIsCreated() throws Exception {
    factorySimulatorService.addItem("ITEM-1001", ItemColor.Red, "MM-ejection");

    JsonNode payload = objectMapper.readTree(
        payloadFactory.createColorDetectionPayload(factorySimulatorService.getSink("MM-ejection").item()));

    assertEquals(0, payload.get("i1_light_barrier").asInt());
    assertEquals(1350, payload.get("i2_color_sensor").asInt());
    assertEquals(1, payload.get("i3_light_barrier").asInt());
    assertEquals("detect_color", payload.get("current_task").asText());
    assertFullRawSorterEnvelope(payload);
  }

  private void assertFullRawSorterEnvelope(JsonNode payload) {
    assertTrue(payload.hasNonNull("id"));
    assertEquals("SM_1", payload.get("station").asText());
    assertTrue(payload.hasNonNull("timestamp"));
    assertTrue(payload.hasNonNull("m1_speed"));
    assertTrue(payload.hasNonNull("o5_valve"));
    assertTrue(payload.hasNonNull("o6_valve"));
    assertTrue(payload.hasNonNull("o7_valve"));
    assertTrue(payload.hasNonNull("o8_compressor"));
    assertTrue(payload.hasNonNull("current_state"));
    assertTrue(payload.hasNonNull("current_sub_task"));
  }
}
