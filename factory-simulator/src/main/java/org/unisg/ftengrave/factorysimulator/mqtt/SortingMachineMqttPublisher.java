package org.unisg.ftengrave.factorysimulator.mqtt;

import org.eclipse.paho.client.mqttv3.MqttException;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.factorysimulator.domain.Item;

@Component
@ConditionalOnProperty(prefix = "factory.mqtt", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SortingMachineMqttPublisher extends AbstractMqttPublisher
    implements MovementTriggeredMqttPublisher, SorterColorDetectionMqttPublisher {

  private final SortingMachineMqttPayloadFactory payloadFactory;

  public SortingMachineMqttPublisher(
      FactoryMqttProperties properties,
      SortingMachineMqttPayloadFactory payloadFactory) {
    super(properties.getBrokerUrl(), properties.getSorterClientId(), properties.getSorterTopic());
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

  @Override
  public void publishDetectedColor(Item item) {
    try {
      publishPayload(payloadFactory.createColorDetectionPayload(item));
    } catch (MqttException exception) {
      // Color detection is best-effort. The HTTP command still needs to complete and move the item.
    }
  }
}
