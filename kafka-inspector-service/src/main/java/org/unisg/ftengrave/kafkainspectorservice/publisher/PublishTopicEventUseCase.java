package org.unisg.ftengrave.kafkainspectorservice.publisher;

import com.fasterxml.jackson.databind.JsonNode;

public interface PublishTopicEventUseCase {

  void publish(String key, JsonNode payload);
}
