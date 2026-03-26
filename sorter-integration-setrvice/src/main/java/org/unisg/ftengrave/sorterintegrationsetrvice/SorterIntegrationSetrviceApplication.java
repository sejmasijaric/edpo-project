package org.unisg.ftengrave.sorterintegrationsetrvice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication(scanBasePackages = {
    "org.unisg.ftengrave.sorterintegrationsetrvice",
    "org.unisg.mqttkafkabridge"
})
public class SorterIntegrationSetrviceApplication {

  public static void main(String[] args) {
    SpringApplication.run(SorterIntegrationSetrviceApplication.class, args);
  }
}
