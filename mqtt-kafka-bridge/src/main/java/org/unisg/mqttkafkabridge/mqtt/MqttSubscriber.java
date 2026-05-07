package org.unisg.mqttkafkabridge.mqtt;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import org.eclipse.paho.client.mqttv3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.unisg.mqttkafkabridge.filter.MqttEventFilter;
import org.unisg.mqttkafkabridge.kafka.KafkaEventPublisher;

@Component
@ConditionalOnProperty(name = "mqtt.bridge.enabled", havingValue = "true", matchIfMissing = true)
public class MqttSubscriber implements MqttCallback {

  private static final Logger LOGGER = LoggerFactory.getLogger(MqttSubscriber.class);

  private final KafkaEventPublisher kafkaEventPublisher;
  private final ObjectProvider<MqttEventFilter<?>> mqttEventFilter;

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

  @Value("${mqtt.bridge.raw-forwarding-enabled:false}")
  private boolean rawForwardingEnabled;

  private MqttClient mqttClient;

  public MqttSubscriber(
      KafkaEventPublisher kafkaEventPublisher,
      ObjectProvider<MqttEventFilter<?>> mqttEventFilter) {
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
    LOGGER.info("MQTT bridge subscribed to {} on {}. Raw forwarding enabled: {}",
        topic, brokerUrl, rawForwardingEnabled);
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
    LOGGER.warn("MQTT bridge connection lost", cause);
  }

  @Override
  public void messageArrived(String topic, MqttMessage message) {
    String rawPayload = new String(message.getPayload(), StandardCharsets.UTF_8);
    if (rawForwardingEnabled) {
      LOGGER.debug("Forwarding raw MQTT message from {} to Kafka", topic);
      kafkaEventPublisher.publishRaw(topic, rawPayload);
      return;
    }

    MqttEventFilter<?> filter = mqttEventFilter.getIfAvailable();
    if (filter == null) {
      LOGGER.warn("Ignoring MQTT message from {} because no MqttEventFilter is configured", topic);
      return;
    }
    filter.filter(topic, rawPayload).ifPresent(kafkaEventPublisher::publish);
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
  }
}
