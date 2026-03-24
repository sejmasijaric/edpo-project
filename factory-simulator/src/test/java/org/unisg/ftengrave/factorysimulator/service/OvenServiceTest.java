package org.unisg.ftengrave.factorysimulator.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import org.junit.jupiter.api.Test;

class OvenServiceTest {

  @Test
  void usesTheConfiguredDefaultBurnDurationWhenNoTimeIsProvided() {
    FactorySimulationProperties properties = new FactorySimulationProperties();
    properties.setOvenBurnDuration(Duration.ZERO);
    OvenService service = new OvenService(properties, "ov_1", 500, 100);

    OvenService.OvenExecution execution = service.burn("ov_1", null);

    assertFalse(service.getStatus().performingAction());
    assertEquals("Idle", service.getStatus().phase());
    assertTrue(execution.processTime().compareTo(Duration.ofSeconds(1)) < 0);
  }

  @Test
  void rejectsUnsupportedMachineNames() {
    FactorySimulationProperties properties = new FactorySimulationProperties();
    OvenService service = new OvenService(properties, "ov_1", 500, 100);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> service.burn("ov_2", 0));

    assertEquals("Unsupported oven machine: ov_2", exception.getMessage());
  }

  @Test
  void rejectsNegativeBurnTimes() {
    FactorySimulationProperties properties = new FactorySimulationProperties();
    OvenService service = new OvenService(properties, "ov_1", 500, 100);

    IllegalArgumentException exception = assertThrows(
        IllegalArgumentException.class,
        () -> service.burn("ov_1", -1));

    assertEquals("Burn time must be zero or greater", exception.getMessage());
  }
}
