package org.unisg.ftengrave.factorysimulator.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "factory.mqtt", name = "enabled", havingValue = "true", matchIfMissing = true)
public class MillingMachineMqttPublisher extends AbstractMqttPublisher {

  private final MillingMachineMqttPayloadFactory payloadFactory;

  public MillingMachineMqttPublisher(
      FactoryMqttProperties properties,
      MillingMachineMqttPayloadFactory payloadFactory) {
    super(
        properties.getBrokerUrl(),
        properties.getMillingMachineClientId(),
        properties.getMillingMachineTopic());
    this.payloadFactory = payloadFactory;
  }

  @Scheduled(fixedDelayString = "${factory.mqtt.publish-interval:2s}")
  void publish() throws MqttException {
    publishPayload(payloadFactory.createPayload());
  }
}
