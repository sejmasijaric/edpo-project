package com.example.frontendspringbootservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardProxyController {

    private final RestClient restClient;
    private final String dashboardBaseUrl;

    public DashboardProxyController(
            @Value("${app.dashboard.service.base-url:http://dashboard-service:8105}") String dashboardBaseUrl) {
        this.restClient = RestClient.create();
        this.dashboardBaseUrl = dashboardBaseUrl;
    }

    @GetMapping(value = "/metrics", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> metrics(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(dashboardBaseUrl)
                .path("/api/dashboard/metrics");
        if (from != null) uri.queryParam("from", from);
        if (to != null) uri.queryParam("to", to);

        try {
            String body = restClient.get()
                    .uri(uri.build().toUri())
                    .retrieve()
                    .body(String.class);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body);
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(HttpStatusCode.valueOf(ex.getStatusCode().value()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(ex.getResponseBodyAsString());
        } catch (Exception ex) {
            return ResponseEntity.status(503)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(toJsonError("Dashboard service unavailable: " + ex.getMessage()));
        }
    }

    private String toJsonError(String message) {
        return "{\"error\":\"" + message.replace("\"", "\\\"") + "\"}";
    }
}
