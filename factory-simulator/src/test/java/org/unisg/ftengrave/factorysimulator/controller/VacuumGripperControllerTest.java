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
import org.unisg.ftengrave.factorysimulator.service.VacuumGripperService;

class VacuumGripperControllerTest {

  private FactorySimulatorService factorySimulatorService;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    factorySimulatorService = new FactorySimulatorService();

    FactorySimulationProperties properties = new FactorySimulationProperties();
    properties.setMovementDelay(java.time.Duration.ZERO);

    mockMvc = MockMvcBuilders.standaloneSetup(
        new VacuumGripperController(new VacuumGripperService(factorySimulatorService, properties)))
        .build();
  }

  @Test
  void transportsItemsUsingTheMachineSpecificMapping() throws Exception {
    factorySimulatorService.addItem("ITEM-1001", ItemColor.Red, "SINK-S2");

    mockMvc.perform(get("/vgr/pick_up_and_transport")
            .param("machine", "vgr_1")
            .param("start", "sink_2")
            .param("end", "oven"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            {
              "attributes":[],
              "link":"http://localhost/vgr/pick_up_and_transport"
            }
            """))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("\"start_time\":\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("\"end_time\":\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("\"process_time\":\"0:00:00.")));
  }

  @Test
  void succeedsWhenNoItemExistsAtTheStartSink() throws Exception {
    mockMvc.perform(get("/vgr/pick_up_and_transport")
            .param("machine", "vgr_1")
            .param("start", "sink_2")
            .param("end", "oven"))
        .andExpect(status().isOk());
  }

  @Test
  void rejectsUnknownMachines() throws Exception {
    mockMvc.perform(get("/vgr/pick_up_and_transport")
            .param("machine", "vgr_2")
            .param("start", "sink_2")
            .param("end", "oven"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Unsupported vacuum gripper machine: vgr_2"));
  }
}
