package org.unisg.ftengrave.factoryeventstreams.dto;

public record TranslatedMachineEvent(
    String topic,
    String key,
    String payloadJson,
    String itemIdentifier,
    String station,
    String timestamp,
    String sourceTopic) {
}
