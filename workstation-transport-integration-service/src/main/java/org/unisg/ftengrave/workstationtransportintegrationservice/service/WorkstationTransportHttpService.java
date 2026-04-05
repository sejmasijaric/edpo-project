package org.unisg.ftengrave.workstationtransportintegrationservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class WorkstationTransportHttpService {

  private final RestTemplate restTemplate;
  private final String protocol;
  private final String host;
  private final int port;
  private final String transportPath;
  private final String machine;
  private final String start;
  private final String end;

  public WorkstationTransportHttpService(
      RestTemplate restTemplate,
      @Value("${workstation-transport.protocol}") String protocol,
      @Value("${workstation-transport.host}") String host,
      @Value("${workstation-transport.port}") int port,
      @Value("${workstation-transport.transport-path}") String transportPath,
      @Value("${workstation-transport.machine}") String machine,
      @Value("${workstation-transport.start}") String start,
      @Value("${workstation-transport.end}") String end) {
    this.restTemplate = restTemplate;
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    this.transportPath = transportPath;
    this.machine = machine;
    this.start = start;
    this.end = end;
  }

  public void moveItemFromEngraverToPolishingMachine() {
    String uri = UriComponentsBuilder.newInstance()
        .scheme(protocol)
        .host(host)
        .port(port)
        .path(transportPath)
        .queryParam("machine", machine)
        .queryParam("start", start)
        .queryParam("end", end)
        .build()
        .toUriString();

    restTemplate.exchange(uri, HttpMethod.GET, null, String.class);
  }
}
