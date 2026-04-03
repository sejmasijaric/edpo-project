package org.unisg.ftengrave.workstationtransportintegrationservice.service;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class WorkstationTransportHttpServiceTest {

  @Test
  void moveItemFromEngraverToPolishingMachineCallsConfiguredEndpoint() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    WorkstationTransportHttpService workstationTransportHttpService =
        new WorkstationTransportHttpService(
            restTemplate,
            "http",
            "factory-simulator",
            8081,
            "/wt/pick_up_and_transport",
            "wt_1",
            "oven",
            "milling_machine");

    server.expect(once(), requestTo(
            "http://factory-simulator:8081/wt/pick_up_and_transport?machine=wt_1&start=oven&end=milling_machine"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    workstationTransportHttpService.moveItemFromEngraverToPolishingMachine();

    server.verify();
  }
}
