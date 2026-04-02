package org.unisg.ftengrave.factorysimulator.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "factory.mqtt", name = "enabled", havingValue = "true", matchIfMissing = true)
public class OvenMqttPublisher extends AbstractMqttPublisher {

  private final OvenMqttPayloadFactory payloadFactory;

  public OvenMqttPublisher(
      FactoryMqttProperties properties,
      OvenMqttPayloadFactory payloadFactory) {
    super(properties.getBrokerUrl(), properties.getOvenClientId(), properties.getOvenTopic());
    this.payloadFactory = payloadFactory;
  }

  @Scheduled(fixedDelayString = "${factory.mqtt.publish-interval:2s}")
  void publish() throws MqttException {
    publishPayload(payloadFactory.createPayload());
  }
}
