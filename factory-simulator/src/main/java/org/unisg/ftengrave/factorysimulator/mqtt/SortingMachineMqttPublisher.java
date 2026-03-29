package org.unisg.ftengrave.factorysimulator.mqtt;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "factory.mqtt", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SortingMachineMqttPublisher implements MqttCallback {

  private final FactoryMqttProperties properties;
  private final SortingMachineMqttPayloadFactory payloadFactory;
  private MqttClient mqttClient;

  public SortingMachineMqttPublisher(
      FactoryMqttProperties properties,
      SortingMachineMqttPayloadFactory payloadFactory) {
    this.properties = properties;
    this.payloadFactory = payloadFactory;
  }

  @PostConstruct
  void connect() throws MqttException {
    mqttClient = new MqttClient(properties.getBrokerUrl(), properties.getClientId());
    mqttClient.setCallback(this);
    connectIfNecessary();
  }

  @Scheduled(fixedDelayString = "${factory.mqtt.publish-interval:2s}")
  void publish() throws MqttException {
    if (!connectIfNecessary()) {
      return;
    }

    MqttMessage message = new MqttMessage(
        payloadFactory.createPayload().getBytes(StandardCharsets.UTF_8));
    message.setQos(0);
    mqttClient.publish(properties.getTopic(), message);
  }

  private boolean connectIfNecessary() throws MqttException {
    if (mqttClient == null) {
      return false;
    }
    if (mqttClient.isConnected()) {
      return true;
    }

    MqttConnectOptions options = new MqttConnectOptions();
    options.setAutomaticReconnect(true);
    options.setCleanSession(true);
    try {
      mqttClient.connect(options);
      return true;
    } catch (MqttException exception) {
      return false;
    }
  }

  @PreDestroy
  void disconnect() throws MqttException {
    if (mqttClient != null && mqttClient.isConnected()) {
      mqttClient.disconnect();
    }
    if (mqttClient != null) {
      mqttClient.close();
    }
  }

  @Override
  public void connectionLost(Throwable cause) {
  }

  @Override
  public void messageArrived(String topic, MqttMessage message) {
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
  }
}
