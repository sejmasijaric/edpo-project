package org.unisg.ftengrave.vacuumgripperintegrationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "org.unisg.ftengrave.vacuumgripperintegrationservice",
        "org.unisg.mqttkafkabridge"
})
public class VacuumGripperIntegrationServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(VacuumGripperIntegrationServiceApplication.class, args);
  }
}
