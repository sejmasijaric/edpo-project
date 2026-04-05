package org.unisg.ftengrave.polishingmachineintegrationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "org.unisg.ftengrave.polishingmachineintegrationservice",
        "org.unisg.mqttkafkabridge"
})
public class PolishingMachineIntegrationServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(PolishingMachineIntegrationServiceApplication.class, args);
  }
}
