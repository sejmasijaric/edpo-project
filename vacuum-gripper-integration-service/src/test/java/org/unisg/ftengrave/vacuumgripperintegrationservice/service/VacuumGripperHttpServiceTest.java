package org.unisg.ftengrave.vacuumgripperintegrationservice.service;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class VacuumGripperHttpServiceTest {

  @Test
  void moveItemFromInputToEngraverCallsConfiguredEndpoint() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    VacuumGripperHttpService vacuumGripperHttpService =
        new VacuumGripperHttpService(
            restTemplate,
            "http",
            "factory-simulator",
            8081,
            "/vgr/pick_up_and_transport",
            "vgr_1",
            "delivery_pick_up_station",
            "oven");

    server.expect(once(), requestTo(
            "http://factory-simulator:8081/vgr/pick_up_and_transport?machine=vgr_1&start=delivery_pick_up_station&end=oven"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    vacuumGripperHttpService.moveItemFromInputToEngraver();

    server.verify();
  }
}
