package org.unisg.ftengrave.polishingmachineintegrationservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class PolishingMachineHttpService {

  private final RestTemplate restTemplate;
  private final String protocol;
  private final String host;
  private final int port;
  private final String runPath;
  private final String machine;
  private final int processingTime;
  private final String start;
  private final String end;

  public PolishingMachineHttpService(
      RestTemplate restTemplate,
      @Value("${polishing-machine.protocol}") String protocol,
      @Value("${polishing-machine.host}") String host,
      @Value("${polishing-machine.port}") int port,
      @Value("${polishing-machine.run-path}") String runPath,
      @Value("${polishing-machine.machine}") String machine,
      @Value("${polishing-machine.processing-time}") int processingTime,
      @Value("${polishing-machine.start}") String start,
      @Value("${polishing-machine.end}") String end) {
    this.restTemplate = restTemplate;
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    this.runPath = runPath;
    this.machine = machine;
    this.processingTime = processingTime;
    this.start = start;
    this.end = end;
  }

  public void runPolishing() {
    String uri = UriComponentsBuilder.newInstance()
        .scheme(protocol)
        .host(host)
        .port(port)
        .path(runPath)
        .queryParam("machine", machine)
        .queryParam("time", processingTime)
        .queryParam("start", start)
        .queryParam("end", end)
        .build()
        .toUriString();

    restTemplate.exchange(uri, HttpMethod.GET, null, String.class);
  }
}
