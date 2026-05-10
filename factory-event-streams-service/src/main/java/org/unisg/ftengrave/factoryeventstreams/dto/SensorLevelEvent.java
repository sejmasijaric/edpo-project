package org.unisg.ftengrave.factoryeventstreams.dto;

import java.util.LinkedHashMap;
import java.util.Map;

public class SensorLevelEvent {

  private String originalEventId;
  private String sourceTopic;
  private String station;
  private String timestamp;
  private String sensorName;
  private String sensorValue;
  private Map<String, String> metadata = new LinkedHashMap<>();

  public SensorLevelEvent() {
  }

  public SensorLevelEvent(
      String originalEventId,
      String sourceTopic,
      String station,
      String timestamp,
      String sensorName,
      String sensorValue,
      Map<String, String> metadata) {
    this.originalEventId = originalEventId;
    this.sourceTopic = sourceTopic;
    this.station = station;
    this.timestamp = timestamp;
    this.sensorName = sensorName;
    this.sensorValue = sensorValue;
    this.metadata = metadata;
  }

  public String getOriginalEventId() {
    return originalEventId;
  }

  public void setOriginalEventId(String originalEventId) {
    this.originalEventId = originalEventId;
  }

  public String getSourceTopic() {
    return sourceTopic;
  }

  public void setSourceTopic(String sourceTopic) {
    this.sourceTopic = sourceTopic;
  }

  public String getStation() {
    return station;
  }

  public void setStation(String station) {
    this.station = station;
  }

  public String getTimestamp() {
    return timestamp;
  }

  public void setTimestamp(String timestamp) {
    this.timestamp = timestamp;
  }

  public String getSensorName() {
    return sensorName;
  }

  public void setSensorName(String sensorName) {
    this.sensorName = sensorName;
  }

  public String getSensorValue() {
    return sensorValue;
  }

  public void setSensorValue(String sensorValue) {
    this.sensorValue = sensorValue;
  }

  public Map<String, String> getMetadata() {
    return metadata;
  }

  public void setMetadata(Map<String, String> metadata) {
    this.metadata = metadata;
  }

  public String sensorKey() {
    return station + ":" + sensorName;
  }
}
