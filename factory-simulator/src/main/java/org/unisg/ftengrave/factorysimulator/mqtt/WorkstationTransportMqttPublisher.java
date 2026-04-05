package org.unisg.ftengrave.factorysimulator.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "factory.mqtt", name = "enabled", havingValue = "true", matchIfMissing = true)
public class WorkstationTransportMqttPublisher extends AbstractMqttPublisher
    implements MovementTriggeredMqttPublisher {

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

  @Override
  public void publishForMovement() {
    try {
      publish();
    } catch (MqttException exception) {
      // Scheduled publishing already tolerates connection issues; movement-triggered publishing
      // should behave the same and never block simulator state updates.
    }
  }
}
