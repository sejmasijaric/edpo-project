package org.unisg.ftengrave.workstationtransportintegrationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "org.unisg.ftengrave.workstationtransportintegrationservice",
        "org.unisg.mqttkafkabridge"
})
public class WorkstationTransportIntegrationServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(WorkstationTransportIntegrationServiceApplication.class, args);
  }
}
