package org.unisg.ftengrave.orderorchestrator.adapter.out.kafka.dto;

import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;

public record PerformQcCommandDto(String itemIdentifier, ItemColor targetColor) {
}
