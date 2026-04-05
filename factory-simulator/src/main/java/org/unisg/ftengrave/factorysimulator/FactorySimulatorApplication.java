package org.unisg.ftengrave.factorysimulator;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.unisg.ftengrave.factorysimulator.mqtt.FactoryMqttProperties;

@EnableScheduling
@EnableConfigurationProperties(FactoryMqttProperties.class)
@SpringBootApplication
public class FactorySimulatorApplication {

  public static void main(String[] args) {
    SpringApplication.run(FactorySimulatorApplication.class, args);
  }
}
