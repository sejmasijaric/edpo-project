package org.unisg.ftengrave.factoryeventstreams.dto;

public record ItemStationAssignment(
    String itemIdentifier,
    String station,
    long validFromTimestamp) {
}
