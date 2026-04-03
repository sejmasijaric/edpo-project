package org.unisg.ftengrave.qcservice.adapter.in.kafka.dto;

import org.unisg.ftengrave.qcservice.domain.ItemColor;

public record PerformQcCommandDto(String itemIdentifier, ItemColor targetColor) {
}
