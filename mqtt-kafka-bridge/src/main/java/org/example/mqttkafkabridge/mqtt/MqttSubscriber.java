package org.example.mqttkafkabridge.mqtt;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.nio.charset.StandardCharsets;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.example.mqttkafkabridge.kafka.KafkaEventPublisher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class MqttSubscriber implements MqttCallback {

  private final KafkaEventPublisher kafkaEventPublisher;

  @Value("${mqtt.broker.url}")
  private String brokerUrl;

  @Value("${mqtt.topic}")
  private String topic;

  @Value("${mqtt.client-id:mqtt-kafka-bridge}")
  private String clientId;

  private MqttClient mqttClient;

  public MqttSubscriber(KafkaEventPublisher kafkaEventPublisher) {
    this.kafkaEventPublisher = kafkaEventPublisher;
  }

  @PostConstruct
  public void start() throws MqttException {
    mqttClient = new MqttClient(brokerUrl, clientId);
    mqttClient.setCallback(this);

    MqttConnectOptions options = new MqttConnectOptions();
    options.setAutomaticReconnect(true);
    options.setCleanSession(true);

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
    // TODO: add filtering/transforming of events before publishing to kafka
    // TODO: use choreography here, not orchestration
    kafkaEventPublisher.publish(new String(message.getPayload(), StandardCharsets.UTF_8));
  }

  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
  }
}
