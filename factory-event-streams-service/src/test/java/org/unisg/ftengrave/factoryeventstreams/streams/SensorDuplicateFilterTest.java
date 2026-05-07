package org.unisg.ftengrave.factoryeventstreams.streams;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.unisg.ftengrave.factoryeventstreams.dto.SensorLevelEvent;

class SensorDuplicateFilterTest {

  private final SensorDuplicateFilter duplicateFilter = new SensorDuplicateFilter();

  @Test
  void detectsConsecutiveDuplicateSensorValues() {
    SensorLevelEvent event = new SensorLevelEvent(
        "evt-1", "FTFactory/SM_1", "SM_1", "2026-04-02T10:15:30Z",
        "i3_light_barrier", "0", Map.of());

    assertTrue(duplicateFilter.isDuplicate("0", event));
    assertFalse(duplicateFilter.isDuplicate("1", event));
    assertFalse(duplicateFilter.isDuplicate(null, event));
  }
}
