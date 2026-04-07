package com.example.frontendspringbootservice.dto;

import com.example.frontendspringbootservice.model.OrderStatus;

public class UpdateStatusRequest {
    private OrderStatus status;

    public OrderStatus getStatus() { return status; }
    public void setStatus(OrderStatus status) { this.status = status; }
}
