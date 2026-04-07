package com.example.frontendspringbootservice.controller;

import com.example.frontendspringbootservice.model.Order;
import com.example.frontendspringbootservice.model.OrderColor;
import com.example.frontendspringbootservice.model.OrderStatus;
import com.example.frontendspringbootservice.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.NoSuchElementException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
class OrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private OrderService orderService;

    private Order sampleOrder(String id, OrderColor color, OrderStatus status, String engravedText) {
        return new Order(id, color, engravedText, status, Instant.parse("2026-04-07T10:00:00Z"));
    }

    // --- POST /api/orders ---

    @Test
    void createOrder_returnsCreated() throws Exception {
        Order order = sampleOrder("abc-123", OrderColor.RED, OrderStatus.TODO, null);
        when(orderService.createOrder(any())).thenReturn(order);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"abc-123","color":"red","createdAt":"2026-04-07T10:00:00Z"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value("abc-123"))
                .andExpect(jsonPath("$.color").value("red"))
                .andExpect(jsonPath("$.status").value("To Do"));
    }

    @Test
    void createOrder_withEngravedText() throws Exception {
        Order order = sampleOrder("abc-456", OrderColor.BLUE, OrderStatus.TODO, "My Tag");
        when(orderService.createOrder(any())).thenReturn(order);

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"abc-456","color":"blue","engravedText":"My Tag","createdAt":"2026-04-07T10:00:00Z"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.engravedText").value("My Tag"));
    }

    @Test
    void createOrder_missingColorReturnsBadRequest() throws Exception {
        when(orderService.createOrder(any()))
                .thenThrow(new IllegalArgumentException("Color is required"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"abc-789"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Color is required"));
    }

    @Test
    void createOrder_textTooLongReturnsBadRequest() throws Exception {
        when(orderService.createOrder(any()))
                .thenThrow(new IllegalArgumentException("Engraved text must be 20 characters or less"));

        mockMvc.perform(post("/api/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"id":"abc-000","color":"red","engravedText":"This text is way too long!!"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Engraved text must be 20 characters or less"));
    }

    // --- GET /api/orders ---

    @Test
    void getAllOrders_returnsList() throws Exception {
        List<Order> orders = List.of(
                sampleOrder("o1", OrderColor.RED, OrderStatus.TODO, null),
                sampleOrder("o2", OrderColor.BLUE, OrderStatus.DONE, "Test")
        );
        when(orderService.getAllOrders()).thenReturn(orders);

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].id").value("o1"))
                .andExpect(jsonPath("$[1].id").value("o2"));
    }

    @Test
    void getAllOrders_emptyList() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of());

        mockMvc.perform(get("/api/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    // --- GET /api/orders/{id} ---

    @Test
    void getOrder_returnsOrder() throws Exception {
        Order order = sampleOrder("find-me", OrderColor.WHITE, OrderStatus.IN_PROGRESS, "Engraved");
        when(orderService.getOrder("find-me")).thenReturn(order);

        mockMvc.perform(get("/api/orders/find-me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("find-me"))
                .andExpect(jsonPath("$.color").value("white"))
                .andExpect(jsonPath("$.status").value("In Progress"))
                .andExpect(jsonPath("$.engravedText").value("Engraved"));
    }

    @Test
    void getOrder_notFoundReturns404() throws Exception {
        when(orderService.getOrder("ghost"))
                .thenThrow(new NoSuchElementException("Order not found: ghost"));

        mockMvc.perform(get("/api/orders/ghost"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Order not found: ghost"));
    }

    // --- PATCH /api/orders/{id}/status ---

    @Test
    void updateStatus_returnsUpdatedOrder() throws Exception {
        Order order = sampleOrder("u1", OrderColor.RED, OrderStatus.IN_PROGRESS, null);
        when(orderService.updateOrderStatus(eq("u1"), eq(OrderStatus.IN_PROGRESS))).thenReturn(order);

        mockMvc.perform(patch("/api/orders/u1/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"In Progress"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("In Progress"));
    }

    @Test
    void updateStatus_invalidTransitionReturnsBadRequest() throws Exception {
        when(orderService.updateOrderStatus(eq("u2"), eq(OrderStatus.DONE)))
                .thenThrow(new IllegalArgumentException("Invalid status transition from To Do to Done"));

        mockMvc.perform(patch("/api/orders/u2/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"Done"}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Invalid status transition from To Do to Done"));
    }

    @Test
    void updateStatus_notFoundReturns404() throws Exception {
        when(orderService.updateOrderStatus(eq("missing"), any()))
                .thenThrow(new NoSuchElementException("Order not found: missing"));

        mockMvc.perform(patch("/api/orders/missing/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"status":"In Progress"}
                                """))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("Order not found: missing"));
    }
}
