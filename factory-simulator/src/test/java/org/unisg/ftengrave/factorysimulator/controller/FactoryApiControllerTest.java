package org.unisg.ftengrave.factorysimulator.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulationProperties;
import org.unisg.ftengrave.factorysimulator.service.FactorySimulatorService;
import org.unisg.ftengrave.factorysimulator.service.OneWayPointToPointTransportService;
import org.unisg.ftengrave.factorysimulator.service.OvenService;
import org.unisg.ftengrave.factorysimulator.service.VacuumGripperService;

class FactoryApiControllerTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    FactorySimulatorService factorySimulatorService = new FactorySimulatorService();
    FactorySimulationProperties properties = new FactorySimulationProperties();
    VacuumGripperService vgrService = new VacuumGripperService(
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
    VacuumGripperService wtService = new VacuumGripperService(
        factorySimulatorService,
        properties,
        "wt_1",
        "WT-Hold",
        620,
        120,
        java.util.Map.of(
            "oven", "VGR-oven",
            "milling_machine", "MM-initial"));
    OneWayPointToPointTransportService sorterService = new OneWayPointToPointTransportService(
        factorySimulatorService,
        properties,
        "sm_1",
        "initial",
        "SM-I",
        java.util.List.of("MM-ejection"),
        "SM-Hold",
        880,
        410);
    OvenService ovenService = new OvenService(
        properties,
        "ov_1",
        500,
        100);
    mockMvc = MockMvcBuilders.standaloneSetup(
        new FactoryApiController(
            factorySimulatorService,
            vgrService,
            wtService,
            sorterService,
            ovenService)).build();
  }

  @Test
  void returnsItems() throws Exception {
    mockMvc.perform(get("/api/items"))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));
  }

  @Test
  void returnsVacuumGripperStatus() throws Exception {
    mockMvc.perform(get("/api/vgr/status"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            {
              "machine":"vgr_1",
              "performingAction":false,
              "phase":"Idle",
              "x":530,
              "y":360
            }
            """));
  }

  @Test
  void returnsWorkstationTransportStatus() throws Exception {
    mockMvc.perform(get("/api/wt/status"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            {
              "machine":"wt_1",
              "performingAction":false,
              "phase":"Idle",
              "x":620,
              "y":120
            }
            """));
  }

  @Test
  void returnsSorterStatus() throws Exception {
    mockMvc.perform(get("/api/sm/status"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            {
              "machine":"sm_1",
              "performingAction":false,
              "phase":"Belt off",
              "x":880,
              "y":410
            }
            """));
  }

  @Test
  void returnsOvenStatus() throws Exception {
    mockMvc.perform(get("/api/ov/status"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            {
              "machine":"ov_1",
              "performingAction":false,
              "phase":"Idle",
              "x":500,
              "y":100
            }
            """));
  }

  @Test
  void returnsSinks() throws Exception {
    mockMvc.perform(get("/api/sinks"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            [
              {"id":"MM-ejection","x":860,"y":120,"item":null},
              {"id":"MM-initial","x":740,"y":180,"item":null},
              {"id":"SINK-I1","x":530,"y":550,"item":null},
              {"id":"SINK-I2","x":670,"y":550,"item":null},
              {"id":"SINK-S1","x":780,"y":400,"item":null},
              {"id":"SINK-S2","x":780,"y":470,"item":null},
              {"id":"SINK-S3","x":780,"y":540,"item":null},
              {"id":"SM-I","x":850,"y":330,"item":null},
              {"id":"SM-Hold","x":880,"y":470,"item":null},
              {"id":"VGR-Hold","x":530,"y":420,"item":null},
              {"id":"VGR-oven","x":500,"y":180,"item":null},
              {"id":"WT-Hold","x":620,"y":180,"item":null}
            ]
            """));
  }

  @Test
  void addsItems() throws Exception {
    mockMvc.perform(post("/api/items")
            .param("itemId", "ITEM-2001")
            .param("color", "Blue")
            .param("sinkId", "SINK-S1"))
        .andExpect(status().isCreated());

    mockMvc.perform(get("/api/items"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            [
              {"id":"ITEM-2001","color":"Blue","sinkId":"SINK-S1"}
            ]
            """));
  }

  @Test
  void movesItems() throws Exception {
    mockMvc.perform(post("/api/items")
            .param("itemId", "ITEM-1001")
            .param("color", "Red")
            .param("sinkId", "SINK-I1"))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/api/items/ITEM-1001/move").param("targetSinkId", "SINK-S1"))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/items"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            [
              {"id":"ITEM-1001","color":"Red","sinkId":"SINK-S1"}
            ]
            """));
  }

  @Test
  void deletesItems() throws Exception {
    mockMvc.perform(post("/api/items")
            .param("itemId", "ITEM-1002")
            .param("color", "White")
            .param("sinkId", "SINK-S2"))
        .andExpect(status().isCreated());

    mockMvc.perform(delete("/api/items/ITEM-1002"))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/items"))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));
  }

  @Test
  void mapsUnknownItemsToNotFound() throws Exception {
    mockMvc.perform(delete("/api/items/ITEM-404"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Unknown item: ITEM-404"));
  }

  @Test
  void mapsIllegalMovesToBadRequest() throws Exception {
    mockMvc.perform(post("/api/items")
            .param("itemId", "ITEM-1001")
            .param("color", "Red")
            .param("sinkId", "SINK-I1"))
        .andExpect(status().isCreated());
    mockMvc.perform(post("/api/items")
            .param("itemId", "ITEM-1002")
            .param("color", "Blue")
            .param("sinkId", "SINK-I2"))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/api/items/ITEM-1001/move").param("targetSinkId", "SINK-I2"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Target sink already contains an item"));
  }

  @Test
  void mapsIllegalAddsToBadRequest() throws Exception {
    mockMvc.perform(post("/api/items")
            .param("itemId", "ITEM-2001")
            .param("color", "Blue")
            .param("sinkId", "SINK-I1"))
        .andExpect(status().isCreated());

    mockMvc.perform(post("/api/items")
            .param("itemId", "ITEM-2002")
            .param("color", "Blue")
            .param("sinkId", "SINK-I1"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Target sink already contains an item"));
  }
}
