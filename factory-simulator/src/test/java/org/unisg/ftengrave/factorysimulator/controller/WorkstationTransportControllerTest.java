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

class WorkstationTransportControllerTest {

  private FactorySimulatorService factorySimulatorService;
  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    factorySimulatorService = new FactorySimulatorService();

    FactorySimulationProperties properties = new FactorySimulationProperties();
    properties.setMovementDelay(java.time.Duration.ZERO);

    mockMvc = MockMvcBuilders.standaloneSetup(
        new WorkstationTransportController(new VacuumGripperService(
            factorySimulatorService,
            properties,
            "wt_1",
            "WT-Hold",
            620,
            120,
            java.util.Map.of(
                "oven", "VGR-oven",
                "milling_machine", "MM-initial"))))
        .build();
  }

  @Test
  void transportsItemsUsingTheWorkstationTransportMapping() throws Exception {
    factorySimulatorService.addItem("ITEM-1001", ItemColor.Red, "MM-initial");

    mockMvc.perform(get("/wt/pick_up_and_transport")
            .param("machine", "wt_1")
            .param("start", "milling_machine")
            .param("end", "oven"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            {
              "attributes":[],
              "link":"http://localhost/wt/pick_up_and_transport"
            }
            """))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("\"start_time\":\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("\"end_time\":\"")))
        .andExpect(content().string(org.hamcrest.Matchers.containsString("\"process_time\":\"0:00:00.")));
  }

  @Test
  void rejectsUnknownMachines() throws Exception {
    mockMvc.perform(get("/wt/pick_up_and_transport")
            .param("machine", "wt_2")
            .param("start", "milling_machine")
            .param("end", "oven"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Unsupported vacuum gripper machine: wt_2"));
  }

  @Test
  void succeedsWhenTheTargetSinkIsOccupied() throws Exception {
    factorySimulatorService.addItem("ITEM-1001", ItemColor.Red, "MM-initial");
    factorySimulatorService.addItem("ITEM-1002", ItemColor.Blue, "VGR-oven");

    mockMvc.perform(get("/wt/pick_up_and_transport")
            .param("machine", "wt_1")
            .param("start", "milling_machine")
            .param("end", "oven"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            {
              "attributes":[],
              "link":"http://localhost/wt/pick_up_and_transport"
            }
            """));
  }
}
