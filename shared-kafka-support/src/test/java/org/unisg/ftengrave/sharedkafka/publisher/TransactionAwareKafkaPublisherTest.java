package org.unisg.ftengrave.sharedkafka.publisher;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Proxy;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class TransactionAwareKafkaPublisherTest {

  @Test
  void publishAfterCommitOrNowSendsImmediatelyWithoutTransactionSynchronization() {
    RecordingKafkaOperations kafkaOperations = new RecordingKafkaOperations();
    TestPublisher publisher = new TestPublisher(kafkaOperations.proxy);

    publisher.publish("topic", "key", "payload");

    assertThat(kafkaOperations.sentRecords).containsExactly(new SentRecord("topic", "key", "payload"));
  }

  @Test
  void publishAfterCommitOrNowDefersSendUntilAfterCommit() {
    RecordingKafkaOperations kafkaOperations = new RecordingKafkaOperations();
    TestPublisher publisher = new TestPublisher(kafkaOperations.proxy);

    TransactionSynchronizationManager.initSynchronization();
    try {
      publisher.publish("topic", "key", "payload");

      assertThat(kafkaOperations.sentRecords).isEmpty();

      for (TransactionSynchronization synchronization : TransactionSynchronizationManager.getSynchronizations()) {
        synchronization.afterCommit();
      }

      assertThat(kafkaOperations.sentRecords).containsExactly(new SentRecord("topic", "key", "payload"));
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

  private static final class RecordingKafkaOperations {
    private final List<SentRecord> sentRecords = new ArrayList<>();
    private final KafkaOperations<String, String> proxy =
        (KafkaOperations<String, String>)
            Proxy.newProxyInstance(
                KafkaOperations.class.getClassLoader(),
                new Class<?>[] {KafkaOperations.class},
                (unusedProxy, method, args) -> {
                  if ("send".equals(method.getName()) && args != null && args.length == 3) {
                    sentRecords.add(new SentRecord((String) args[0], (String) args[1], (String) args[2]));
                    return null;
                  }
                  if ("toString".equals(method.getName())) {
                    return "RecordingKafkaOperations";
                  }
                  if ("hashCode".equals(method.getName())) {
                    return System.identityHashCode(this);
                  }
                  if ("equals".equals(method.getName())) {
                    return unusedProxy == args[0];
                  }
                  return null;
                });
  }

  private record SentRecord(String topic, String key, String payload) {
  }
}
