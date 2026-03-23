package org.unisg.ftengrave.factorysimulator.service;

import java.time.Duration;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "factory.simulation")
public class FactorySimulationProperties {

  private Duration movementDelay = Duration.ofSeconds(4);

  public Duration getMovementDelay() {
    return movementDelay;
  }

  public void setMovementDelay(Duration movementDelay) {
    this.movementDelay = movementDelay;
  }
}
