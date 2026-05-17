package com.example.frontendspringbootservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/qc")
public class QcTaskController {

    private static final Logger log = LoggerFactory.getLogger(QcTaskController.class);
    private static final String CHECK_QUALITY_TASK = "Check Quality";

    private final RestClient restClient;
    private final String qcServiceBaseUrl;

    public QcTaskController(
            @Value("${app.qc.service.base-url:http://qc-service:8100}") String qcServiceBaseUrl) {
        this.restClient = RestClient.create();
        this.qcServiceBaseUrl = qcServiceBaseUrl;
    }

    @PostMapping(value = "/check-quality/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> completeCheckQuality(
            @RequestParam String itemId,
            @RequestParam boolean passed) {
        try {
            String taskId = findCheckQualityTaskId(itemId);
            if (taskId == null) {
                return ResponseEntity.status(404)
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(toJsonError("No open Check Quality task for item " + itemId));
            }

            URI completeUri = UriComponentsBuilder.fromUriString(qcServiceBaseUrl)
                    .path("/engine-rest/task/{id}/complete")
                    .buildAndExpand(taskId)
                    .toUri();

            Map<String, Object> body = Map.of(
                    "variables", Map.of(
                            "qcPassed", Map.of("value", passed, "type", "Boolean")));

            restClient.post()
                    .uri(completeUri)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(body)
                    .retrieve()
                    .toBodilessEntity();

            log.info("Completed Check Quality for item {} with qcPassed={}", itemId, passed);
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"ok\":true,\"itemId\":\"" + itemId + "\",\"passed\":" + passed + "}");
        } catch (RestClientResponseException ex) {
            return ResponseEntity.status(HttpStatusCode.valueOf(ex.getStatusCode().value()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(toJsonError(safeBody(ex)));
        } catch (Exception ex) {
            return ResponseEntity.status(503)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(toJsonError("QC service unavailable: " + ex.getMessage()));
        }
    }

    @SuppressWarnings("unchecked")
    private String findCheckQualityTaskId(String itemId) {
        URI taskUri = UriComponentsBuilder.fromUriString(qcServiceBaseUrl)
                .path("/engine-rest/task")
                .queryParam("processInstanceBusinessKey", itemId)
                .queryParam("name", CHECK_QUALITY_TASK)
                .build()
                .toUri();
        List<Map<String, Object>> tasks = restClient.get()
                .uri(taskUri)
                .retrieve()
                .body(List.class);
        if (tasks == null || tasks.isEmpty()) {
            return null;
        }
        return String.valueOf(tasks.get(0).get("id"));
    }

    private String safeBody(RestClientResponseException ex) {
        try {
            return ex.getResponseBodyAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String toJsonError(String message) {
        return "{\"error\":\"" + (message == null ? "" : message.replace("\"", "\\\"")) + "\"}";
    }
}
