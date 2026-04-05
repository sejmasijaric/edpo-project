package org.unisg.mqttkafkabridge.filter;

import java.util.Optional;

public interface MqttEventFilter<T> {
    Optional<T> filter(String topic, String rawPayload);
}