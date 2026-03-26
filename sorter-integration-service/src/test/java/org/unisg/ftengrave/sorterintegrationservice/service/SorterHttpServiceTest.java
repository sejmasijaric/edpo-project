package org.unisg.ftengrave.sorterintegrationservice.service;

import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class SorterHttpServiceTest {

  @Test
  void detectColorCallsConfiguredSorterEndpoint() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    SorterHttpService sorterHttpService =
        new SorterHttpService(
            restTemplate,
            "http",
            "192.168.0.21",
            5000,
            "/sm/detect_color",
            "/sm/sort",
            "sm_1",
            "initial");

    server.expect(once(), requestTo("http://192.168.0.21:5000/sm/detect_color?machine=sm_1"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    sorterHttpService.detectColor();

    server.verify();
  }

  @Test
  void sortToSinkCallsConfiguredSorterEndpoint() {
    RestTemplate restTemplate = new RestTemplate();
    MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
    SorterHttpService sorterHttpService =
        new SorterHttpService(
            restTemplate,
            "http",
            "192.168.0.21",
            5000,
            "/sm/detect_color",
            "/sm/sort",
            "sm_1",
            "initial");

    server.expect(once(), requestTo(
            "http://192.168.0.21:5000/sm/sort?machine=sm_1&start=initial&predefined_ejection_location=sink_2"))
        .andExpect(method(HttpMethod.GET))
        .andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));

    sorterHttpService.sortToSink("sink_2");

    server.verify();
  }
}
