package org.unisg.ftengrave.sharedkafka.publisher;

import org.springframework.kafka.core.KafkaOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

public abstract class TransactionAwareKafkaPublisher<K, V> {

  private final KafkaOperations<K, V> kafkaOperations;

  protected TransactionAwareKafkaPublisher(KafkaOperations<K, V> kafkaOperations) {
    this.kafkaOperations = kafkaOperations;
  }

  protected void publishAfterCommitOrNow(Runnable sendAction) {
    if (TransactionSynchronizationManager.isSynchronizationActive()) {
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          sendAction.run();
        }
      });
      return;
    }

    sendAction.run();
  }

  protected void send(String topic, V payload) {
    kafkaOperations.send(topic, payload);
  }

  protected void send(String topic, K key, V payload) {
    kafkaOperations.send(topic, key, payload);
  }
}
