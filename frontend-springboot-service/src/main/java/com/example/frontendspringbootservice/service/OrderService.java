package com.example.frontendspringbootservice.service;

import com.example.frontendspringbootservice.dto.CreateOrderRequest;
import com.example.frontendspringbootservice.model.Order;
import com.example.frontendspringbootservice.model.OrderStatus;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OrderService {

    private final Map<String, Order> orders = new ConcurrentHashMap<>();
    private final KafkaTemplate<String, Order> kafkaTemplate;

    private static final String TOPIC = "order-events";

    public OrderService(KafkaTemplate<String, Order> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public Order createOrder(CreateOrderRequest request) {
        if (request.getId() == null || request.getId().isBlank()) {
            throw new IllegalArgumentException("Order ID is required");
        }
        if (request.getColor() == null) {
            throw new IllegalArgumentException("Color is required");
        }
        if (request.getEngravedText() != null && request.getEngravedText().length() > 20) {
            throw new IllegalArgumentException("Engraved text must be 20 characters or less");
        }

        Order order = new Order();
        order.setId(request.getId());
        order.setColor(request.getColor());
        order.setEngravedText(request.getEngravedText());
        order.setStatus(OrderStatus.TODO);
        order.setCreatedAt(request.getCreatedAt() != null ? request.getCreatedAt() : Instant.now());

        orders.put(order.getId(), order);
        kafkaTemplate.send(TOPIC, order.getId(), order);

        return order;
    }

    public List<Order> getAllOrders() {
        return new ArrayList<>(orders.values());
    }

    public Order getOrder(String id) {
        Order order = orders.get(id);
        if (order == null) {
            throw new NoSuchElementException("Order not found: " + id);
        }
        return order;
    }

    public Order updateOrderStatus(String id, OrderStatus newStatus) {
        Order order = getOrder(id);
        validateStatusTransition(order.getStatus(), newStatus);

        order.setStatus(newStatus);
        kafkaTemplate.send(TOPIC, order.getId(), order);

        return order;
    }

    private void validateStatusTransition(OrderStatus current, OrderStatus target) {
        boolean valid = switch (current) {
            case TODO -> target == OrderStatus.IN_PROGRESS;
            case IN_PROGRESS -> target == OrderStatus.DONE || target == OrderStatus.ERROR;
            case ERROR -> target == OrderStatus.IN_PROGRESS;
            case DONE -> false;
        };
        if (!valid) {
            throw new IllegalArgumentException(
                    "Invalid status transition from " + current.getValue() + " to " + target.getValue());
        }
    }
}
