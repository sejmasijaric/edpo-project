package org.unisg.ftengrave.qcservice;

import org.camunda.bpm.spring.boot.starter.annotation.EnableProcessApplication;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {
        "org.unisg.ftengrave.qcservice",
        "org.unisg.mqttkafkabridge"
})
@EnableProcessApplication("qc-service")
public class QcServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(QcServiceApplication.class, args);
  }

}
