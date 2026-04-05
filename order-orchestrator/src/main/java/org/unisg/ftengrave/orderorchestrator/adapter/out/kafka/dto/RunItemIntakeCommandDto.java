package org.unisg.ftengrave.orderorchestrator.adapter.out.kafka.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;

public record RunItemIntakeCommandDto(
        @JsonAlias("eventType") String commandType,
        String itemIdentifier,
        ItemColor targetColor) {
}
