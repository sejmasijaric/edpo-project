package org.unisg.ftengrave.orderorchestrator.adapter.in.kafka.dto;

public record OrderCreatedEventDto(String itemIdentifier, String targetColor) {
}
