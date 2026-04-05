package org.unisg.ftengrave.engraverintegrationservice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class EngraverHttpService {

  private final RestTemplate restTemplate;
  private final String protocol;
  private final String host;
  private final int port;
  private final String runPath;
  private final String machine;

  public EngraverHttpService(
      RestTemplate restTemplate,
      @Value("${engraver.protocol}") String protocol,
      @Value("${engraver.host}") String host,
      @Value("${engraver.port}") int port,
      @Value("${engraver.run-path}") String runPath,
      @Value("${engraver.machine}") String machine) {
    this.restTemplate = restTemplate;
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    this.runPath = runPath;
    this.machine = machine;
  }

  public void runEngraving() {
    String uri = UriComponentsBuilder.newInstance()
        .scheme(protocol)
        .host(host)
        .port(port)
        .path(runPath)
        .queryParam("machine", machine)
        .build()
        .toUriString();

    restTemplate.exchange(uri, HttpMethod.GET, null, String.class);
  }
}
