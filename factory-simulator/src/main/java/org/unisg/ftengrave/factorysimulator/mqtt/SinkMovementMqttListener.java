package org.unisg.ftengrave.factorysimulator.mqtt;

import java.util.List;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.factorysimulator.service.SinkItemRemovedEvent;

@Component
public class SinkMovementMqttListener {

  private final List<MovementTriggeredMqttPublisher> publishers;

  public SinkMovementMqttListener(List<MovementTriggeredMqttPublisher> publishers) {
    this.publishers = List.copyOf(publishers);
  }

  @EventListener
  public void onSinkItemRemoved(SinkItemRemovedEvent event) {
    for (MovementTriggeredMqttPublisher publisher : publishers) {
      publisher.publishForMovement();
    }
  }
}
