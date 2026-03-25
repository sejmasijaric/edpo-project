package org.unisg.ftengrave.sorterintegrationsetrvice.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class SorterHttpService {

  private final RestTemplate restTemplate;
  private final String protocol;
  private final String host;
  private final int port;
  private final String sortPath;
  private final String machine;
  private final String start;

  public SorterHttpService(
      RestTemplate restTemplate,
      @Value("${sorter.protocol}") String protocol,
      @Value("${sorter.host}") String host,
      @Value("${sorter.port}") int port,
      @Value("${sorter.sort-path}") String sortPath,
      @Value("${sorter.machine}") String machine,
      @Value("${sorter.start}") String start) {
    this.restTemplate = restTemplate;
    this.protocol = protocol;
    this.host = host;
    this.port = port;
    this.sortPath = sortPath;
    this.machine = machine;
    this.start = start;
  }

  public void sortToSink(String sinkIdentifier) {
    String uri = UriComponentsBuilder.newInstance()
        .scheme(protocol)
        .host(host)
        .port(port)
        .path(sortPath)
        .queryParam("machine", machine)
        .queryParam("start", start)
        .queryParam("predefined_ejection_location", sinkIdentifier)
        .build()
        .toUriString();

    restTemplate.exchange(uri, HttpMethod.GET, null, String.class);
  }
}
