package org.unisg.ftengrave.engraverintegrationservice.service;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class EngraverHttpServiceTest {

  @Test
  void runEngravingCallsConfiguredEndpoint() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    EngraverHttpService engraverHttpService =
        new EngraverHttpService(
            restTemplate,
            "http",
            "factory-simulator",
            8081,
            "/ov/burn",
            "ov_1");

    server.expect(once(), requestTo("http://factory-simulator:8081/ov/burn?machine=ov_1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    engraverHttpService.runEngraving();

    server.verify();
  }
}
