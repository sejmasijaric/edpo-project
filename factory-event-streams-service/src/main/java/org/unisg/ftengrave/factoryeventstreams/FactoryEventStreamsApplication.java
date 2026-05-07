package org.unisg.ftengrave.factoryeventstreams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafkaStreams
@SpringBootApplication
public class FactoryEventStreamsApplication {

  public static void main(String[] args) {
    SpringApplication.run(FactoryEventStreamsApplication.class, args);
  }
}
