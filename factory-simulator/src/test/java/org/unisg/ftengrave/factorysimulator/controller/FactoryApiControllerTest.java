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
import org.unisg.ftengrave.factorysimulator.service.FactorySimulatorService;

class FactoryApiControllerTest {

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders.standaloneSetup(
        new FactoryApiController(new FactorySimulatorService())).build();
  }

  @Test
  void returnsItems() throws Exception {
    mockMvc.perform(get("/api/items"))
        .andExpect(status().isOk())
        .andExpect(content().json("[]"));
  }

  @Test
  void returnsSinks() throws Exception {
    mockMvc.perform(get("/api/sinks"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            [
              {"id":"MM-Eject","x":860,"y":120,"item":null},
              {"id":"MM-Init","x":740,"y":180,"item":null},
              {"id":"SINK-I1","x":530,"y":550,"item":null},
              {"id":"SINK-I2","x":670,"y":550,"item":null},
              {"id":"SINK-S1","x":780,"y":400,"item":null},
              {"id":"SINK-S2","x":780,"y":470,"item":null},
              {"id":"SINK-S3","x":780,"y":540,"item":null},
              {"id":"SM-I","x":850,"y":330,"item":null},
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
