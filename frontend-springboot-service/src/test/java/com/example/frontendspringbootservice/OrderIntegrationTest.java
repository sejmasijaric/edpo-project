package com.example.frontendspringbootservice;

import com.example.frontendspringbootservice.model.Order;
import com.example.frontendspringbootservice.model.OrderColor;
import com.example.frontendspringbootservice.model.OrderStatus;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;

import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.kafka.KafkaContainer;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
@AutoConfigureMockMvc

class OrderIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private KafkaContainer kafkaContainer;

    // --- Full order lifecycle ---

    @Test
    void fullOrderLifecycle_createAndProgressThroughStatuses() throws Exception {
        // Create order
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson("lifecycle-1", "red", null)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("lifecycle-1"))
                .andExpect(jsonPath("$.color").value("red"))
                .andExpect(jsonPath("$.status").value("To Do"));

        // Retrieve it
        mockMvc.perform(get("/api/orders/lifecycle-1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.color").value("red"));

        // Start processing
        mockMvc.perform(patch("/api/orders/lifecycle-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"In Progress\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("In Progress"));

        // Complete
        mockMvc.perform(patch("/api/orders/lifecycle-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"Done\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Done"));

        // Verify in list
        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItems("lifecycle-1")));
    }

    @Test
    void fullOrderLifecycle_errorAndRetry() throws Exception {
        createOrder("retry-1", "blue", "Retry Tag");
        patchStatus("retry-1", "In Progress");
        patchStatus("retry-1", "Error");

        // Verify error state
        mockMvc.perform(get("/api/orders/retry-1"))
                .andExpect(jsonPath("$.status").value("Error"));

        // Retry
        mockMvc.perform(patch("/api/orders/retry-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"In Progress\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("In Progress"));

        // Complete after retry
        mockMvc.perform(patch("/api/orders/retry-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"Done\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("Done"));
    }

    // --- Create order ---

    @Test
    void createOrder_withEngravedText() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson("engrave-1", "white", "Hello World")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.engravedText").value("Hello World"))
                .andExpect(jsonPath("$.createdAt").isNotEmpty());
    }

    @Test
    void createOrder_missingColorReturns400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"id\":\"bad-1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Color is required"));
    }

    @Test
    void createOrder_engravedTextTooLongReturns400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson("bad-2", "red", "This text is way too long!!")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Engraved text must be 20 characters or less"));
    }

    @Test
    void createOrder_missingIdReturns400() throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"color\":\"red\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Order ID is required"));
    }

    // --- Get orders ---

    @Test
    void getOrders_returnsCreatedOrders() throws Exception {
        createOrder("multi-1", "red", null);
        createOrder("multi-2", "blue", "Tag");
        createOrder("multi-3", "white", null);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[*].id", hasItems("multi-1", "multi-2", "multi-3")));
    }

    @Test
    void getOrder_notFoundReturns404() throws Exception {
        mockMvc.perform(get("/api/orders/nonexistent"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Order not found: nonexistent"));
    }

    // --- Status transitions ---

    @Test
    void updateStatus_invalidTransitionReturns400() throws Exception {
        createOrder("invalid-1", "red", null);

        // To Do -> Done is not valid
        mockMvc.perform(patch("/api/orders/invalid-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"Done\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Invalid status transition")));
    }

    @Test
    void updateStatus_doneIsTerminal() throws Exception {
        createOrder("terminal-1", "red", null);
        patchStatus("terminal-1", "In Progress");
        patchStatus("terminal-1", "Done");

        // Done -> In Progress is not valid
        mockMvc.perform(patch("/api/orders/terminal-1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"In Progress\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error", containsString("Invalid status transition")));
    }

    @Test
    void updateStatus_nonexistentOrderReturns404() throws Exception {
        mockMvc.perform(patch("/api/orders/ghost/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"In Progress\"}"))
                .andExpect(status().isNotFound());
    }

    // --- Kafka verification ---

    @Test
    void createOrder_producesEventToKafka() throws Exception {
        try (var consumer = createKafkaConsumer()) {
            consumer.subscribe(List.of("order-events"));

            createOrder("kafka-1", "blue", "Kafka Tag");

            String matchingValue = null;
            var deadline = Instant.now().plusSeconds(10);
            while (matchingValue == null && Instant.now().isBefore(deadline)) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));
                for (var record : records) {
                    if ("kafka-1".equals(record.key())) {
                        matchingValue = record.value();
                    }
                }
            }

            assertThat(matchingValue).isNotNull();
            assertThat(matchingValue).contains("\"color\":\"blue\"");
            assertThat(matchingValue).contains("\"engravedText\":\"Kafka Tag\"");
            assertThat(matchingValue).contains("\"status\":\"To Do\"");
        }
    }

    @Test
    void statusUpdate_producesEventToKafka() throws Exception {
        try (var consumer = createKafkaConsumer()) {
            consumer.subscribe(List.of("order-events"));

            createOrder("kafka-2", "red", null);
            patchStatus("kafka-2", "In Progress");

            // Poll until we get the status update event (second event)
            int totalRecords = 0;
            String lastValue = null;
            var deadline = Instant.now().plusSeconds(10);
            while (totalRecords < 2 && Instant.now().isBefore(deadline)) {
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofSeconds(2));
                for (var record : records) {
                    totalRecords++;
                    lastValue = record.value();
                }
            }

            assertThat(totalRecords).isGreaterThanOrEqualTo(2);
            assertThat(lastValue).isNotNull();
            assertThat(lastValue).contains("\"status\":\"In Progress\"");
        }
    }

    // --- JSON serialization ---

    @Test
    void jsonSerialization_enumsUseFrontendFormat() throws Exception {
        createOrder("json-1", "white", null);

        var result = mockMvc.perform(get("/api/orders/json-1"))
                .andExpect(status().isOk())
                .andReturn();
        String body = result.getResponse().getContentAsString();

        // Verify enum values match frontend format (not Java enum names)
        assertThat(body).contains("\"color\":\"white\"");
        assertThat(body).contains("\"status\":\"To Do\"");
        // createdAt should be ISO-8601, not a timestamp number
        assertThat(body).containsPattern("\"createdAt\":\"\\d{4}-\\d{2}-\\d{2}T");
    }

    // --- Helpers ---

    private void createOrder(String id, String color, String engravedText) throws Exception {
        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson(id, color, engravedText)))
                .andExpect(status().isCreated());
    }

    private void patchStatus(String id, String status) throws Exception {
        mockMvc.perform(patch("/api/orders/" + id + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"" + status + "\"}"))
                .andExpect(status().isOk());
    }

    private String orderJson(String id, String color, String engravedText) {
        String engravedPart = engravedText != null
                ? ",\"engravedText\":\"" + engravedText + "\""
                : "";
        return String.format(
                "{\"id\":\"%s\",\"color\":\"%s\"%s,\"createdAt\":\"%s\"}",
                id, color, engravedPart, Instant.now().toString());
    }

    private KafkaConsumer<String, String> createKafkaConsumer() {
        var props = Map.<String, Object>of(
                ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaContainer.getBootstrapServers(),
                ConsumerConfig.GROUP_ID_CONFIG, "integration-test-" + System.nanoTime(),
                ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest",
                ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class,
                ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class
        );
        return new KafkaConsumer<>(props);
    }
}
