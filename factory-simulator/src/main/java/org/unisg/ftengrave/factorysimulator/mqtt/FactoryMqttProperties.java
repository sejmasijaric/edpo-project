package org.unisg.ftengrave.factorysimulator.mqtt;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "factory.mqtt")
public class FactoryMqttProperties {

  private boolean enabled = true;
  private String brokerUrl = "tcp://localhost:1883";
  private String topic = "FTFactory/SM_1";
  private String clientId = "factory-simulator";
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

  public String getTopic() {
    return topic;
  }

  public void setTopic(String topic) {
    this.topic = topic;
  }

  public String getClientId() {
    return clientId;
  }

  public void setClientId(String clientId) {
    this.clientId = clientId;
  }

  public Duration getPublishInterval() {
    return publishInterval;
  }

  public void setPublishInterval(Duration publishInterval) {
    this.publishInterval = publishInterval;
  }
}
