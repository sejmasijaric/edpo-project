package com.example.frontendspringbootservice.model;

import java.time.Instant;

public class Order {
    private String id;
    private OrderColor color;
    private String engravedText;
    private OrderStatus status;
    private Instant createdAt;

    public Order() {}

    public Order(String id, OrderColor color, String engravedText, OrderStatus status, Instant createdAt) {
        this.id = id;
        this.color = color;
        this.engravedText = engravedText;
        this.status = status;
        this.createdAt = createdAt;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public OrderColor getColor() { return color; }
    public void setColor(OrderColor color) { this.color = color; }

    public String getEngravedText() { return engravedText; }
    public void setEngravedText(String engravedText) { this.engravedText = engravedText; }

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
