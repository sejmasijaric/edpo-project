package org.unisg.mqttkafkabridge.mqtt;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import org.eclipse.paho.client.mqttv3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.unisg.mqttkafkabridge.filter.MqttEventFilter;
import org.unisg.mqttkafkabridge.kafka.KafkaEventPublisher;

@Component
@ConditionalOnProperty(name = "mqtt.bridge.enabled", havingValue = "true", matchIfMissing = true)
public class MqttSubscriber implements MqttCallback {

  private final KafkaEventPublisher kafkaEventPublisher;
  private final MqttEventFilter<?> mqttEventFilter;

  @Value("${mqtt.broker.url}")
  private String brokerUrl;

  @Value("${mqtt.topic}")
  private String topic;

  @Value("${mqtt.client-id:mqtt-kafka-bridge}")
  private String clientId;

  @Value("${mqtt.username:}")
  private String username;

  @Value("${mqtt.password:}")
  private String password;

  private MqttClient mqttClient;

  public MqttSubscriber(KafkaEventPublisher kafkaEventPublisher, MqttEventFilter<?> mqttEventFilter) {
    this.kafkaEventPublisher = kafkaEventPublisher;
    this.mqttEventFilter = mqttEventFilter;
  }

  @PostConstruct
  public void start() throws MqttException {
    mqttClient = new MqttClient(brokerUrl, clientId);
    mqttClient.setCallback(this);

    MqttConnectOptions options = new MqttConnectOptions();
    options.setAutomaticReconnect(true);
    options.setCleanSession(true);
    if (!username.isBlank()) {
      options.setUserName(username);
    }
    if (!password.isBlank()) {
      options.setPassword(password.toCharArray());
    }

    mqttClient.connect(options);
    mqttClient.subscribe(topic);
  }

  @PreDestroy
  public void stop() throws MqttException {
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
    String rawPayload = new String(message.getPayload(), StandardCharsets.UTF_8);
    Optional<?> filteredEvent = mqttEventFilter.filter(topic, rawPayload);
    filteredEvent.ifPresent(kafkaEventPublisher::publish);
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
  }
}
