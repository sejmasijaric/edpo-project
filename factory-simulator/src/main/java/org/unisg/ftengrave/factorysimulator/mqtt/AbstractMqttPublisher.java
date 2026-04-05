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

abstract class AbstractMqttPublisher implements MqttCallback {

  private final String brokerUrl;
  private final String clientId;
  private final String topic;
  private MqttClient mqttClient;

  protected AbstractMqttPublisher(String brokerUrl, String clientId, String topic) {
    this.brokerUrl = brokerUrl;
    this.clientId = clientId;
    this.topic = topic;
  }

  @PostConstruct
  void connect() throws MqttException {
    mqttClient = new MqttClient(brokerUrl, clientId);
    mqttClient.setCallback(this);
    connectIfNecessary();
  }

  protected void publishPayload(String payload) throws MqttException {
    if (!connectIfNecessary()) {
      return;
    }

    MqttMessage message = new MqttMessage(payload.getBytes(StandardCharsets.UTF_8));
    message.setQos(0);
    mqttClient.publish(topic, message);
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
