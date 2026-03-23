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

class SorterControllerTest {

  private FactorySimulatorService factorySimulatorService;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    factorySimulatorService = new FactorySimulatorService();

    FactorySimulationProperties properties = new FactorySimulationProperties();
    properties.setMovementDelay(java.time.Duration.ZERO);

    mockMvc = MockMvcBuilders.standaloneSetup(
        new SorterController(new OneWayPointToPointTransportService(
            factorySimulatorService,
            properties,
            "sm_1",
            "initial",
            "SM-I",
            java.util.List.of("MM-ejection"),
            "SM-Hold",
            880,
            410)))
        .build();
  }

  @Test
  void sortsItemsUsingTheRequestedEjectionLocation() throws Exception {
    factorySimulatorService.addItem("ITEM-1001", ItemColor.Red, "SM-I");

    mockMvc.perform(get("/sm/sort")
            .param("machine", "sm_1")
            .param("start", "initial")
            .param("predefined_ejection_location", "sink_2"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            {
              "attributes":[{"sink":null}],
              "link":"http://localhost/sm/sort"
            }
            """))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("\"start_time\":\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("\"end_time\":\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("\"process_time\":\"0:00:00.")));
  }

  @Test
  void rejectsUnsupportedStartValues() throws Exception {
    mockMvc.perform(get("/sm/sort")
            .param("machine", "sm_1")
            .param("start", "other")
            .param("predefined_ejection_location", "sink_1"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Unknown start for machine sm_1: other"));
  }

  @Test
  void rejectsUnknownEjectionLocations() throws Exception {
    mockMvc.perform(get("/sm/sort")
            .param("machine", "sm_1")
            .param("start", "initial")
            .param("predefined_ejection_location", "UNKNOWN-SINK"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Unknown predefined ejection location: UNKNOWN-SINK"));
  }
}
