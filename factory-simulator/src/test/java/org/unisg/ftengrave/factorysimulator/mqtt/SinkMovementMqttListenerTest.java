package org.unisg.ftengrave.factorysimulator.mqtt;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factorysimulator.service.SinkItemRemovedEvent;

class SinkMovementMqttListenerTest {

  @Test
  void republishesAllMqttPayloadsWhenAnItemLeavesASink() {
    MovementTriggeredMqttPublisher sorterPublisher = mock(MovementTriggeredMqttPublisher.class);
    MovementTriggeredMqttPublisher millingPublisher = mock(MovementTriggeredMqttPublisher.class);
    SinkMovementMqttListener listener =
        new SinkMovementMqttListener(List.of(sorterPublisher, millingPublisher));

    listener.onSinkItemRemoved(new SinkItemRemovedEvent("SINK-I1"));

    verify(sorterPublisher).publishForMovement();
    verify(millingPublisher).publishForMovement();
  }
}
