package org.unisg.ftengrave.dashboardservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafkaStreams
@SpringBootApplication
public class DashboardServiceApplication {

  public static void main(String[] args) {
    SpringApplication.run(DashboardServiceApplication.class, args);
  }
}
