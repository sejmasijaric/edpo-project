package com.example.frontendspringbootservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/manual-task")
public class ManualTaskController {

    private static final Logger log = LoggerFactory.getLogger(ManualTaskController.class);

    private final RestClient restClient;
    private final Map<String, String> serviceBaseUrls;

    public ManualTaskController(
            @Value("${app.qc.service.base-url:http://qc-service:8100}") String qcBase,
            @Value("${app.order-orchestrator.service.base-url:http://order-orchestrator:8101}") String orderBase,
            @Value("${app.intake.service.base-url:http://intake-service:8103}") String intakeBase,
            @Value("${app.manufacturing.service.base-url:http://manufacturing-service:8104}") String manufBase) {
        this.restClient = RestClient.create();
        this.serviceBaseUrls = new LinkedHashMap<>();
        this.serviceBaseUrls.put("order-orchestrator", orderBase);
        this.serviceBaseUrls.put("qc-service", qcBase);
        this.serviceBaseUrls.put("intake-service", intakeBase);
        this.serviceBaseUrls.put("manufacturing-service", manufBase);
    }

    @PostMapping(value = "/complete", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> complete(
            @RequestParam String itemId,
            @RequestParam(required = false) String taskName) {
        for (Map.Entry<String, String> entry : serviceBaseUrls.entrySet()) {
            String service = entry.getKey();
            String baseUrl = entry.getValue();
            try {
                String taskId = findTaskId(baseUrl, itemId, taskName);
                if (taskId == null) continue;
                completeTask(baseUrl, taskId);
                log.info("Completed manual task {} ({}) on service {}", taskName, itemId, service);
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"ok\":true,\"service\":\"" + service + "\",\"taskId\":\"" + taskId
                                + "\",\"taskName\":\"" + (taskName == null ? "" : taskName) + "\"}");
            } catch (Exception ex) {
                log.debug("Lookup on {} failed: {}", service, ex.getMessage());
            }
        }
        return ResponseEntity.status(404)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"error\":\"No matching Camunda task for item " + itemId + "\"}");
    }

    @SuppressWarnings("unchecked")
    private String findTaskId(String baseUrl, String itemId, String taskName) {
        UriComponentsBuilder uri = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/engine-rest/task")
                .queryParam("processInstanceBusinessKey", itemId);
        if (taskName != null && !taskName.isBlank()) {
            uri.queryParam("name", taskName);
        }
        List<Map<String, Object>> tasks = restClient.get()
                .uri(uri.build().toUri())
                .retrieve()
                .body(List.class);
        if (tasks == null || tasks.isEmpty()) return null;
        return String.valueOf(tasks.get(0).get("id"));
    }

    private void completeTask(String baseUrl, String taskId) {
        URI completeUri = UriComponentsBuilder.fromUriString(baseUrl)
                .path("/engine-rest/task/{id}/complete")
                .buildAndExpand(taskId)
                .toUri();
        restClient.post()
                .uri(completeUri)
                .contentType(MediaType.APPLICATION_JSON)
                .body(Collections.emptyMap())
                .retrieve()
                .toBodilessEntity();
    }
}
