package org.unisg.ftengrave.polishingmachineintegrationservice.service;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class PolishingMachineHttpServiceTest {

  @Test
  void runPolishingCallsConfiguredEndpoint() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    PolishingMachineHttpService polishingMachineHttpService =
        new PolishingMachineHttpService(
            restTemplate,
            "http",
            "factory-simulator",
            8081,
            "/mm/mill",
            "mm_1",
            10,
            "initial",
            "ejection");

    server.expect(once(), requestTo(
            "http://factory-simulator:8081/mm/mill?machine=mm_1&time=10&start=initial&end=ejection"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    polishingMachineHttpService.runPolishing();

    server.verify();
  }
}
