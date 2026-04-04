package org.unisg.ftengrave.factorysimulator.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulationProperties;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulatorService;
import org.unisg.ftengrave.factorysimulator.service.OvenService;

class OvenControllerTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    FactorySimulationProperties properties = new FactorySimulationProperties();
    properties.setOvenBurnDuration(Duration.ZERO);

    mockMvc = MockMvcBuilders.standaloneSetup(
        new OvenController(new OvenService(
            new FactorySimulatorService(),
            properties,
            "ov_1",
            500,
            100)))
        .build();
  }

  @Test
  void burnsUsingTheConfiguredDefaultDurationWhenNoTimeIsProvided() throws Exception {
    mockMvc.perform(get("/ov/burn")
            .param("machine", "ov_1"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            {
              "attributes":[],
              "link":"http://localhost/ov/burn"
            }
            """))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("\"start_time\":\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("\"end_time\":\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("\"process_time\":\"0:00:00.")));
  }

  @Test
  void burnsUsingTheRequestedDuration() throws Exception {
    mockMvc.perform(get("/ov/burn")
            .param("machine", "ov_1")
            .param("time", "0"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            {
              "attributes":[],
              "link":"http://localhost/ov/burn"
            }
            """));
  }

  @Test
  void rejectsUnknownMachines() throws Exception {
    mockMvc.perform(get("/ov/burn")
            .param("machine", "ov_2"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Unsupported oven machine: ov_2"));
  }

  @Test
  void rejectsNegativeBurnTimes() throws Exception {
    mockMvc.perform(get("/ov/burn")
            .param("machine", "ov_1")
            .param("time", "-1"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Burn time must be zero or greater"));
  }
}
