package org.unisg.ftengrave.factoryeventstreams.config;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.streams.errors.MissingSourceTopicException;
import org.apache.kafka.streams.errors.StreamsUncaughtExceptionHandler.StreamThreadExceptionResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.SmartLifecycle;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;
import org.springframework.stereotype.Component;

@Component
public class KafkaStreamsStartupCoordinator implements StreamsBuilderFactoryBeanConfigurer, SmartLifecycle {

  private static final Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsStartupCoordinator.class);

  private final String bootstrapServers;
  private final List<String> requiredSourceTopics;
  private final Duration retryInterval;
  private final Duration checkTimeout;
  private final ScheduledExecutorService scheduler;
  private final AtomicBoolean running = new AtomicBoolean(false);
  private final AtomicBoolean startScheduled = new AtomicBoolean(false);

  private StreamsBuilderFactoryBean streamsBuilderFactoryBean;
  private ScheduledFuture<?> startTask;

  public KafkaStreamsStartupCoordinator(
      @Value("${kafka.bootstrap-servers:${kafka.bootstrap-address}}") String bootstrapServers,
      @Value("${kafka.topic.raw-factory-event}") String rawFactoryEventTopic,
      @Value("${kafka.topic.stage-orchestration}") String stageOrchestrationTopic,
      @Value("${kafka.streams.topic-availability-retry-interval:5s}") Duration retryInterval,
      @Value("${kafka.streams.topic-availability-check-timeout:3s}") Duration checkTimeout) {
    this.bootstrapServers = bootstrapServers;
    this.requiredSourceTopics = List.of(rawFactoryEventTopic, stageOrchestrationTopic);
    this.retryInterval = retryInterval;
    this.checkTimeout = checkTimeout;
    this.scheduler = Executors.newSingleThreadScheduledExecutor(runnable -> {
      Thread thread = new Thread(runnable, "kafka-streams-topic-availability");
      thread.setDaemon(false);
      return thread;
    });
  }

  @Override
  public void configure(StreamsBuilderFactoryBean factoryBean) {
    this.streamsBuilderFactoryBean = factoryBean;
    factoryBean.setAutoStartup(false);
    factoryBean.setStreamsUncaughtExceptionHandler(exception -> {
      if (isMissingSourceTopicException(exception)) {
        LOGGER.warn("Kafka Streams source topic missing during processing; replacing stream thread and retrying");
        return StreamThreadExceptionResponse.REPLACE_THREAD;
      }
      return StreamThreadExceptionResponse.SHUTDOWN_CLIENT;
    });
  }

  @Override
  public void start() {
    if (running.compareAndSet(false, true)) {
      scheduleStartAttempt(Duration.ZERO);
    }
  }

  @Override
  public void stop() {
    running.set(false);
    if (startTask != null) {
      startTask.cancel(true);
    }
    if (streamsBuilderFactoryBean != null && streamsBuilderFactoryBean.isRunning()) {
      streamsBuilderFactoryBean.stop();
    }
    scheduler.shutdownNow();
  }

  @Override
  public boolean isRunning() {
    return running.get();
  }

  private void scheduleStartAttempt(Duration delay) {
    if (!running.get() || !startScheduled.compareAndSet(false, true)) {
      return;
    }
    startTask = scheduler.schedule(this::attemptStart, delay.toMillis(), TimeUnit.MILLISECONDS);
  }

  private void attemptStart() {
    startScheduled.set(false);
    if (!running.get() || streamsBuilderFactoryBean == null || streamsBuilderFactoryBean.isRunning()) {
      return;
    }

    if (!requiredTopicsAvailable()) {
      scheduleStartAttempt(retryInterval);
      return;
    }

    try {
      LOGGER.info("Starting Kafka Streams after source topics became available: {}", requiredSourceTopics);
      streamsBuilderFactoryBean.start();
    } catch (RuntimeException exception) {
      LOGGER.warn("Kafka Streams could not be started yet; retrying in {}", retryInterval, exception);
      scheduleStartAttempt(retryInterval);
    }
  }

  private boolean requiredTopicsAvailable() {
    try (AdminClient adminClient = AdminClient.create(
        Map.of(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers))) {
      Set<String> topics = adminClient.listTopics().names().get(checkTimeout.toMillis(), TimeUnit.MILLISECONDS);
      List<String> missingTopics = requiredSourceTopics.stream()
          .filter(topic -> !topics.contains(topic))
          .toList();
      if (missingTopics.isEmpty()) {
        return true;
      }

      LOGGER.info("Waiting for Kafka source topics before starting streams: missing={}", missingTopics);
      return false;
    } catch (TimeoutException exception) {
      LOGGER.info("Timed out checking Kafka source topics; retrying in {}", retryInterval);
      return false;
    } catch (Exception exception) {
      LOGGER.info("Could not check Kafka source topics; retrying in {}", retryInterval, exception);
      return false;
    }
  }

  private boolean isMissingSourceTopicException(Throwable exception) {
    Throwable current = exception;
    while (current != null) {
      if (current instanceof MissingSourceTopicException) {
        return true;
      }
      current = current.getCause();
    }
    return false;
  }
}
