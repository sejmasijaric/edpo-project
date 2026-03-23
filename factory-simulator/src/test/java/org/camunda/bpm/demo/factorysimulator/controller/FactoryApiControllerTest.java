package org.camunda.bpm.demo.factorysimulator.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.camunda.bpm.demo.factorysimulator.service.FactorySimulatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

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
        .andExpect(content().json("""
            [
              {"id":"ITEM-1001","color":"Red","sinkId":"SINK-A1"},
              {"id":"ITEM-1002","color":"Red","sinkId":"SINK-A2"},
              {"id":"ITEM-1003","color":"White","sinkId":"SINK-B2"}
            ]
            """));
  }

  @Test
  void returnsSinks() throws Exception {
    mockMvc.perform(get("/api/sinks"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            [
              {"id":"SINK-A1","x":120,"y":110,"item":{"id":"ITEM-1001","color":"Red"}},
              {"id":"SINK-A2","x":330,"y":110,"item":{"id":"ITEM-1002","color":"Red"}},
              {"id":"SINK-B1","x":180,"y":300,"item":null},
              {"id":"SINK-B2","x":470,"y":250,"item":{"id":"ITEM-1003","color":"White"}},
              {"id":"SINK-C1","x":620,"y":420,"item":null},
              {"id":"SINK-C2","x":800,"y":180,"item":null}
            ]
            """));
  }

  @Test
  void deletesItems() throws Exception {
    mockMvc.perform(delete("/api/items/ITEM-1002"))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/items"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            [
              {"id":"ITEM-1001","color":"Red","sinkId":"SINK-A1"},
              {"id":"ITEM-1003","color":"White","sinkId":"SINK-B2"}
            ]
            """));
  }

  @Test
  void movesItems() throws Exception {
    mockMvc.perform(post("/api/items/ITEM-1001/move").param("targetSinkId", "SINK-B1"))
        .andExpect(status().isNoContent());

    mockMvc.perform(get("/api/items"))
        .andExpect(status().isOk())
        .andExpect(content().json("""
            [
              {"id":"ITEM-1001","color":"Red","sinkId":"SINK-B1"},
              {"id":"ITEM-1002","color":"Red","sinkId":"SINK-A2"},
              {"id":"ITEM-1003","color":"White","sinkId":"SINK-B2"}
            ]
            """));
  }

  @Test
  void mapsUnknownItemsToNotFound() throws Exception {
    mockMvc.perform(delete("/api/items/ITEM-404"))
        .andExpect(status().isNotFound())
        .andExpect(content().string("Unknown item: ITEM-404"));
  }

  @Test
  void mapsIllegalMovesToBadRequest() throws Exception {
    mockMvc.perform(post("/api/items/ITEM-1001/move").param("targetSinkId", "SINK-A2"))
        .andExpect(status().isBadRequest())
        .andExpect(content().string("Target sink already contains an item"));
  }
}
