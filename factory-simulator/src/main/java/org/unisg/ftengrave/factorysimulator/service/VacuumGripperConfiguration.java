package org.unisg.ftengrave.factorysimulator.service;

import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.unisg.ftengrave.factorysimulator.mqtt.SorterColorDetectionMqttPublisher;

@Configuration
public class VacuumGripperConfiguration {

  @Bean("vgrService")
  public VacuumGripperService vgrService(
      FactorySimulatorService factorySimulatorService,
      FactorySimulationProperties properties) {
    return new VacuumGripperService(
        factorySimulatorService,
        properties,
        "vgr_1",
        "VGR-Hold",
        530,
        360,
        createSinkMapping(
            "oven", "VGR-oven",
            "delivery_pick_up_station", "SINK-I1",
            "end", "SINK-I2",
            "sink_1", "SINK-S1",
            "sink_2", "SINK-S2",
            "sink_3", "SINK-S3"));
  }

  @Bean("wtService")
  public VacuumGripperService wtService(
      FactorySimulatorService factorySimulatorService,
      FactorySimulationProperties properties) {
    return new VacuumGripperService(
        factorySimulatorService,
        properties,
        "wt_1",
        "WT-Hold",
        620,
        120,
        createSinkMapping(
            "oven", "VGR-oven",
            "milling_machine", "MM-initial"));
  }

  @Bean("sorterService")
  public OneWayPointToPointTransportService sorterService(
      FactorySimulatorService factorySimulatorService,
      FactorySimulationProperties properties,
      SorterColorDetectionMqttPublisher sorterColorDetectionMqttPublisher) {
    return new OneWayPointToPointTransportService(
        factorySimulatorService,
        properties,
        "sm_1",
        "initial",
        "SM-I",
        java.util.List.of("MM-ejection"),
        "SM-Hold",
        880,
        410,
        sorterColorDetectionMqttPublisher);
  }

  @Bean("millingMachineService")
  public OneWayPointToPointTransportService millingMachineService(
      FactorySimulatorService factorySimulatorService,
      FactorySimulationProperties properties) {
    return new OneWayPointToPointTransportService(
        factorySimulatorService,
        properties,
        "mm_1",
        "initial",
        "MM-initial",
        java.util.List.of(),
        "MM-ejection",
        0,
        0);
  }

  @Bean("ovenService")
  public OvenService ovenService(
      FactorySimulatorService factorySimulatorService,
      FactorySimulationProperties properties) {
    return new OvenService(
        factorySimulatorService,
        properties,
        "ov_1",
        500,
        100);
  }

  private static Map<String, String> createSinkMapping(String... entries) {
    Map<String, String> mapping = new LinkedHashMap<>();
    for (int index = 0; index < entries.length; index += 2) {
      mapping.put(entries[index], entries[index + 1]);
    }
    return Map.copyOf(mapping);
  }
}
