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
        new VacuumGripperController(new VacuumGripperService(
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
                "sink_3", "SINK-S3"))))
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
  void succeedsWhenTheTargetSinkIsOccupied() throws Exception {
    factorySimulatorService.addItem("ITEM-1001", ItemColor.Red, "SINK-I1");
    factorySimulatorService.addItem("ITEM-1002", ItemColor.Blue, "SINK-I2");

    mockMvc.perform(get("/vgr/pick_up_and_transport")
            .param("machine", "vgr_1")
            .param("start", "start")
            .param("end", "end"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            {
              "attributes":[],
              "link":"http://localhost/vgr/pick_up_and_transport"
            }
            """));
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
