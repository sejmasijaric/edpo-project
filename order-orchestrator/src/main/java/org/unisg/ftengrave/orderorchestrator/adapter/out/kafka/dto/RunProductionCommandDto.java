package org.unisg.ftengrave.orderorchestrator.adapter.out.kafka.dto;

import com.fasterxml.jackson.annotation.JsonAlias;

public record RunProductionCommandDto(
        @JsonAlias("eventType") String commandType,
        String itemIdentifier) {
}
