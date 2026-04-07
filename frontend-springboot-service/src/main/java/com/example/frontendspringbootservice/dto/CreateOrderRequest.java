package com.example.frontendspringbootservice.dto;

import com.example.frontendspringbootservice.model.OrderColor;

import java.time.Instant;

public class CreateOrderRequest {
    private String id;
    private OrderColor color;
    private String engravedText;
    private Instant createdAt;

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public OrderColor getColor() { return color; }
    public void setColor(OrderColor color) { this.color = color; }

    public String getEngravedText() { return engravedText; }
    public void setEngravedText(String engravedText) { this.engravedText = engravedText; }

    public Instant getCreatedAt() { return createdAt; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
