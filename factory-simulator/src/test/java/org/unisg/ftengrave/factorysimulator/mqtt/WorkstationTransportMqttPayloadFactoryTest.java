package org.unisg.ftengrave.factorysimulator.mqtt;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulationProperties;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulatorService;
import org.unisg.ftengrave.factorysimulator.service.VacuumGripperService;

class WorkstationTransportMqttPayloadFactoryTest {

  private final ObjectMapper objectMapper = new ObjectMapper();
  private final VacuumGripperService workstationTransportService = createWorkstationTransportService();
  private final WorkstationTransportMqttPayloadFactory payloadFactory =
      new WorkstationTransportMqttPayloadFactory(
          objectMapper,
          new MqttTimestampFactory(),
          workstationTransportService);

  @Test
  void reportsIdleTaskWhenWorkstationTransportIsIdle() throws Exception {
    JsonNode payload = objectMapper.readTree(payloadFactory.createPayload());

    assertEquals("", payload.get("current_task").asText());
    assertEquals(0.0d, payload.get("current_task_duration").asDouble());
  }

  private VacuumGripperService createWorkstationTransportService() {
    FactorySimulationProperties properties = new FactorySimulationProperties();
    properties.setMovementDelay(Duration.ZERO);

    return new VacuumGripperService(
        new FactorySimulatorService(),
        properties,
        "wt_1",
        "WT-Hold",
        620,
        120,
        java.util.Map.of(
            "oven", "VGR-oven",
            "milling_machine", "MM-initial"));
  }
}
