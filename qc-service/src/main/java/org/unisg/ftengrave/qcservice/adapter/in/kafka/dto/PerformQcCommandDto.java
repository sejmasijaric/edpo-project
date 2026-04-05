package org.unisg.ftengrave.qcservice.adapter.in.kafka.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import org.unisg.ftengrave.qcservice.domain.ItemColor;

public record PerformQcCommandDto(
        @JsonAlias("eventType") String commandType,
        String itemIdentifier,
        ItemColor targetColor) {
}
