package org.unisg.ftengrave.factoryeventstreams.streams;

public enum OrchestrationStation {
  INTAKE("Intake"),
  MANUFACTURING("Manufacturing"),
  OC("OC"),
  UNKNOWN("UNKNOWN");

  private final String value;

  OrchestrationStation(String value) {
    this.value = value;
  }

  public String value() {
    return value;
  }
}
