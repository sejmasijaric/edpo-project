package org.unisg.ftengrave.engraverintegrationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "org.unisg.ftengrave.engraverintegrationservice",
        "org.unisg.mqttkafkabridge"
})
public class EngraverIntegrationServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(EngraverIntegrationServiceApplication.class, args);
  }
}
