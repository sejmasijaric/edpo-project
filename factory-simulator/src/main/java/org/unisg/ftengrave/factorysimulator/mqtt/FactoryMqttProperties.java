package org.unisg.ftengrave.factorysimulator.mqtt;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "factory.mqtt")
public class FactoryMqttProperties {

  private boolean enabled = true;
  private String brokerUrl = "tcp://localhost:1883";
  private String sorterTopic = "FTFactory/SM_1";
  private String sorterClientId = "factory-simulator-sorter";
  private String vacuumGripperTopic = "FTFactory/VGR_1";
  private String vacuumGripperClientId = "factory-simulator-vgr";
  private Duration publishInterval = Duration.ofSeconds(2);

  public boolean isEnabled() {
    return enabled;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
  }

  public String getBrokerUrl() {
    return brokerUrl;
  }

  public void setBrokerUrl(String brokerUrl) {
    this.brokerUrl = brokerUrl;
  }

  public String getSorterTopic() {
    return sorterTopic;
  }

  public void setSorterTopic(String sorterTopic) {
    this.sorterTopic = sorterTopic;
  }

  public String getSorterClientId() {
    return sorterClientId;
  }

  public void setSorterClientId(String sorterClientId) {
    this.sorterClientId = sorterClientId;
  }

  public String getVacuumGripperTopic() {
    return vacuumGripperTopic;
  }

  public void setVacuumGripperTopic(String vacuumGripperTopic) {
    this.vacuumGripperTopic = vacuumGripperTopic;
  }

  public String getVacuumGripperClientId() {
    return vacuumGripperClientId;
  }

  public void setVacuumGripperClientId(String vacuumGripperClientId) {
    this.vacuumGripperClientId = vacuumGripperClientId;
  }

  public Duration getPublishInterval() {
    return publishInterval;
  }

  public void setPublishInterval(Duration publishInterval) {
    this.publishInterval = publishInterval;
  }
}
