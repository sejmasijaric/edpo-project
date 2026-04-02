package org.unisg.ftengrave.factorysimulator.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "factory.mqtt", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WorkstationTransportMqttPublisher extends AbstractMqttPublisher {

  private final WorkstationTransportMqttPayloadFactory payloadFactory;

  public WorkstationTransportMqttPublisher(
      FactoryMqttProperties properties,
      WorkstationTransportMqttPayloadFactory payloadFactory) {
    super(
        properties.getBrokerUrl(),
        properties.getWorkstationTransportClientId(),
        properties.getWorkstationTransportTopic());
    this.payloadFactory = payloadFactory;
  }

  @Scheduled(fixedDelayString = "${factory.mqtt.publish-interval:2s}")
  void publish() throws MqttException {
    publishPayload(payloadFactory.createPayload());
  }
}
