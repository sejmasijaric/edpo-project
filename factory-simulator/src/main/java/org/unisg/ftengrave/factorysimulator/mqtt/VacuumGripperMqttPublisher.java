package org.unisg.ftengrave.factorysimulator.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "factory.mqtt", name = "enabled", havingValue = "true", matchIfMissing = true)
public class VacuumGripperMqttPublisher extends AbstractMqttPublisher {

  private final VacuumGripperMqttPayloadFactory payloadFactory;

  public VacuumGripperMqttPublisher(
      FactoryMqttProperties properties,
      VacuumGripperMqttPayloadFactory payloadFactory) {
    super(
        properties.getBrokerUrl(),
        properties.getVacuumGripperClientId(),
        properties.getVacuumGripperTopic());
    this.payloadFactory = payloadFactory;
  }

  @Scheduled(fixedDelayString = "${factory.mqtt.publish-interval:2s}")
  void publish() throws MqttException {
    publishPayload(payloadFactory.createPayload());
  }
}
