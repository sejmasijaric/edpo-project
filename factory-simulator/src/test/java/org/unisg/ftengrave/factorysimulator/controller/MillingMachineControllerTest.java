package org.unisg.ftengrave.factorysimulator.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.unisg.ftengrave.factorysimulator.domain.ItemColor;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulationProperties;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulatorService;
import org.unisg.ftengrave.factorysimulator.service.OneWayPointToPointTransportService;

class MillingMachineControllerTest {

  private FactorySimulatorService factorySimulatorService;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    factorySimulatorService = new FactorySimulatorService();

    FactorySimulationProperties properties = new FactorySimulationProperties();
    properties.setMovementDelay(java.time.Duration.ZERO);

    mockMvc = MockMvcBuilders.standaloneSetup(
        new MillingMachineController(new OneWayPointToPointTransportService(
            factorySimulatorService,
            properties,
            "mm_1",
            "initial",
            "MM-initial",
            java.util.List.of(),
            "MM-ejection",
            0,
            0)))
        .build();
  }

  @Test
  void movesItemsUsingTheConfiguredPositionMapping() throws Exception {
    factorySimulatorService.addItem("ITEM-1001", ItemColor.Red, "MM-initial");

    mockMvc.perform(get("/mm/move_from_to")
            .param("machine", "mm_1")
            .param("start", "initial")
            .param("end", "ejection"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            {
              "attributes":[],
              "link":"http://localhost/mm/move_from_to"
            }
            """))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("\"start_time\":\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("\"end_time\":\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("\"process_time\":\"0:00:00.")));
  }

  @Test
  void millEndpointProvidesRequestedAliasForPolishingContract() throws Exception {
    factorySimulatorService.addItem("ITEM-1002", ItemColor.Blue, "MM-initial");

    mockMvc.perform(get("/mm/mill")
            .param("machine", "mm_1")
            .param("time", "10")
            .param("start", "initial")
            .param("end", "ejection"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            {
              "attributes":[],
              "link":"http://localhost/mm/mill"
            }
            """))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("\"start_time\":\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("\"end_time\":\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("\"process_time\":\"0:00:00.")));
  }

  @Test
  void rejectsUnknownEndPositions() throws Exception {
    mockMvc.perform(get("/mm/move_from_to")
            .param("machine", "mm_1")
            .param("start", "initial")
            .param("end", "other"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Unknown end for milling machine: other"));
  }

  @Test
  void rejectsUnknownMachines() throws Exception {
    mockMvc.perform(get("/mm/move_from_to")
            .param("machine", "mm_2")
            .param("start", "initial")
            .param("end", "ejection"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Unsupported one way transport machine: mm_2"));
  }
}
