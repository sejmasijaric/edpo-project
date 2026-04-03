package org.unisg.ftengrave.sharedkafka.publisher;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class TransactionAwareKafkaPublisherTest {

  @Test
  void publishAfterCommitOrNowSendsImmediatelyWithoutTransactionSynchronization() {
    @SuppressWarnings("unchecked")
    KafkaOperations<String, String> kafkaOperations = mock(KafkaOperations.class);
    TestPublisher publisher = new TestPublisher(kafkaOperations);

    publisher.publish("topic", "key", "payload");

    verify(kafkaOperations).send("topic", "key", "payload");
  }

  @Test
  void publishAfterCommitOrNowDefersSendUntilAfterCommit() {
    @SuppressWarnings("unchecked")
    KafkaOperations<String, String> kafkaOperations = mock(KafkaOperations.class);
    TestPublisher publisher = new TestPublisher(kafkaOperations);

    TransactionSynchronizationManager.initSynchronization();
    try {
      publisher.publish("topic", "key", "payload");

      verify(kafkaOperations, never()).send("topic", "key", "payload");

      for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
        synchronization.afterCommit();
      }

      verify(kafkaOperations).send("topic", "key", "payload");
    } finally {
      TransactionSynchronizationManager.clearSynchronization();
    }
  }

  private static final class TestPublisher extends TransactionAwareKafkaPublisher<String, String> {

    private TestPublisher(KafkaOperations<String, String> kafkaOperations) {
      super(kafkaOperations);
    }

    private void publish(String topic, String key, String payload) {
      publishAfterCommitOrNow(() -> send(topic, key, payload));
    }
  }
}
