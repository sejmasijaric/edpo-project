package com.example.frontendspringbootservice.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;

@RestController
@RequestMapping("/api/simulator")
public class SimulatorProxyController {

    private final RestClient restClient;
    private final String simulatorBaseUrl;

    public SimulatorProxyController(
            @Value("${app.simulator.service.base-url:http://factory-simulator:8081}") String simulatorBaseUrl) {
        this.restClient = RestClient.create();
        this.simulatorBaseUrl = simulatorBaseUrl;
    }

    @PostMapping(value = "/items", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> insertItem(
            @RequestParam String itemId,
            @RequestParam String color,
            @RequestParam(defaultValue = "SINK-I1") String sinkId) {
        URI addUri = UriComponentsBuilder.fromUriString(simulatorBaseUrl)
                .path("/api/items")
                .queryParam("itemId", itemId)
                .queryParam("color", normalizeColor(color))
                .queryParam("sinkId", sinkId)
                .build()
                .toUri();
        try {
            restClient.post().uri(addUri).retrieve().toBodilessEntity();
            return ok(itemId, sinkId, "inserted");
        } catch (RestClientResponseException ex) {
            String body = safeBody(ex);
            if (isAlreadyExists(ex, body)) {
                // Idempotent: the item is in the factory already. Make sure it sits at the
                // requested intake sink and report success so the worker UI moves on.
                return moveItem(itemId, sinkId)
                        .orElseGet(() -> ok(itemId, sinkId, "already-present"));
            }
            return ResponseEntity.status(HttpStatusCode.valueOf(ex.getStatusCode().value()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(toJsonError(body));
        } catch (Exception ex) {
            return ResponseEntity.status(503)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(toJsonError("Simulator unavailable: " + ex.getMessage()));
        }
    }

    @DeleteMapping(value = "/items/{itemId}", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<String> deleteItem(@PathVariable String itemId) {
        URI uri = UriComponentsBuilder.fromUriString(simulatorBaseUrl)
                .path("/api/items/{id}")
                .buildAndExpand(itemId)
                .toUri();
        try {
            restClient.delete().uri(uri).retrieve().toBodilessEntity();
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_JSON)
                    .body("{\"ok\":true,\"itemId\":\"" + itemId + "\",\"action\":\"deleted\"}");
        } catch (RestClientResponseException ex) {
            // 404 / item-not-present is idempotent success — the worker already pulled it out.
            if (ex.getStatusCode().is4xxClientError()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.APPLICATION_JSON)
                        .body("{\"ok\":true,\"itemId\":\"" + itemId + "\",\"action\":\"absent\"}");
            }
            return ResponseEntity.status(HttpStatusCode.valueOf(ex.getStatusCode().value()))
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(toJsonError(safeBody(ex)));
        } catch (Exception ex) {
            return ResponseEntity.status(503)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(toJsonError("Simulator unavailable: " + ex.getMessage()));
        }
    }

    private java.util.Optional<ResponseEntity<String>> moveItem(String itemId, String targetSinkId) {
        URI moveUri = UriComponentsBuilder.fromUriString(simulatorBaseUrl)
                .path("/api/items/{id}/move")
                .queryParam("targetSinkId", targetSinkId)
                .buildAndExpand(itemId)
                .toUri();
        try {
            restClient.post().uri(moveUri).retrieve().toBodilessEntity();
            return java.util.Optional.of(ok(itemId, targetSinkId, "moved"));
        } catch (Exception ignored) {
            return java.util.Optional.empty();
        }
    }

    private ResponseEntity<String> ok(String itemId, String sinkId, String action) {
        return ResponseEntity.status(201)
                .contentType(MediaType.APPLICATION_JSON)
                .body("{\"ok\":true,\"itemId\":\"" + itemId + "\",\"sinkId\":\"" + sinkId
                        + "\",\"action\":\"" + action + "\"}");
    }

    private boolean isAlreadyExists(RestClientResponseException ex, String body) {
        int status = ex.getStatusCode().value();
        if (status == 409) return true;
        if (body == null) return false;
        String lower = body.toLowerCase();
        return lower.contains("already exists") || lower.contains("duplicate");
    }

    private String safeBody(RestClientResponseException ex) {
        try {
            return ex.getResponseBodyAsString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private String normalizeColor(String color) {
        if (color == null || color.isBlank()) return color;
        String lower = color.toLowerCase();
        return Character.toUpperCase(lower.charAt(0)) + lower.substring(1);
    }

    private String toJsonError(String message) {
        return "{\"error\":\"" + (message == null ? "" : message.replace("\"", "\\\"")) + "\"}";
    }
}
