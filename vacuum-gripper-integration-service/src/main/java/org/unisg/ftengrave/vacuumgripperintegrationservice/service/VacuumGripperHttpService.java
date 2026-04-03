package org.unisg.ftengrave.vacuumgripperintegrationservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class VacuumGripperHttpService {

  private final RestTemplate restTemplate;
  private final String protocol;
  private final String host;
  private final int port;
  private final String transportPath;
  private final String machine;
  private final String inputSink;
  private final String engraverSink;

  public VacuumGripperHttpService(
      RestTemplate restTemplate,
      @Value("${vacuum-gripper.protocol}") String protocol,
      @Value("${vacuum-gripper.host}") String host,
      @Value("${vacuum-gripper.port}") int port,
      @Value("${vacuum-gripper.transport-path}") String transportPath,
      @Value("${vacuum-gripper.machine}") String machine,
      @Value("${vacuum-gripper.input-sink}") String inputSink,
      @Value("${vacuum-gripper.engraver-sink}") String engraverSink) {
    this.restTemplate = restTemplate;
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    this.transportPath = transportPath;
    this.machine = machine;
    this.inputSink = inputSink;
    this.engraverSink = engraverSink;
  }

  public void moveItemFromInputToEngraver() {
    String uri = UriComponentsBuilder.newInstance()
        .scheme(protocol)
        .host(host)
        .port(port)
        .path(transportPath)
        .queryParam("machine", machine)
        .queryParam("start", inputSink)
        .queryParam("end", engraverSink)
        .build()
        .toUriString();

    restTemplate.exchange(uri, HttpMethod.GET, null, String.class);
  }
}
