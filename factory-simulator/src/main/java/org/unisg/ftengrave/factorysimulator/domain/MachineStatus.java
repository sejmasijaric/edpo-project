package org.unisg.ftengrave.factorysimulator.domain;

public record MachineStatus(
    String machine,
    boolean performingAction,
    String phase,
    int x,
    int y) {
}
