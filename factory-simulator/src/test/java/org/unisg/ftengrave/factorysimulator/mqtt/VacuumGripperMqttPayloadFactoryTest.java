package org.unisg.ftengrave.factorysimulator.mqtt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factorysimulator.domain.ItemColor;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulationProperties;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulatorService;
import org.unisg.ftengrave.factorysimulator.service.VacuumGripperService;

class VacuumGripperMqttPayloadFactoryTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final FactorySimulatorService factorySimulatorService = new FactorySimulatorService();
  private final VacuumGripperService vacuumGripperService = createVacuumGripperService();
  private final VacuumGripperMqttPayloadFactory payloadFactory =
      new VacuumGripperMqttPayloadFactory(
          objectMapper,
          new MqttTimestampFactory(),
          factorySimulatorService,
          vacuumGripperService);

  @Test
  void reportsOpenLightBarriersAndIdleTaskWhenBothInputSinksAreEmpty() throws Exception {
    JsonNode payload = objectMapper.readTree(payloadFactory.createPayload());

    assertEquals(1, payload.get("i7_light_barrier").asInt());
    assertEquals(1, payload.get("i4_light_barrier").asInt());
    assertEquals("", payload.get("current_task").asText());
    assertEquals(0.0d, payload.get("current_task_duration").asDouble());
  }

  @Test
  void reportsClosedLightBarriersWhenItemsArePresentAtTheVgrInputSinks() throws Exception {
    factorySimulatorService.addItem("ITEM-1001", ItemColor.Red, "SINK-I1");
    factorySimulatorService.addItem("ITEM-1002", ItemColor.Blue, "SINK-I2");

    JsonNode payload = objectMapper.readTree(payloadFactory.createPayload());

    assertEquals(0, payload.get("i7_light_barrier").asInt());
    assertEquals(0, payload.get("i4_light_barrier").asInt());
  }

  private VacuumGripperService createVacuumGripperService() {
    FactorySimulationProperties properties = new FactorySimulationProperties();
    properties.setMovementDelay(Duration.ZERO);

    return new VacuumGripperService(
        factorySimulatorService,
        properties,
        "vgr_1",
        "VGR-Hold",
        530,
        360,
        java.util.Map.of(
            "oven", "VGR-oven",
            "start", "SINK-I1",
            "end", "SINK-I2",
            "sink_1", "SINK-S1",
            "sink_2", "SINK-S2",
            "sink_3", "SINK-S3"));
  }
}
