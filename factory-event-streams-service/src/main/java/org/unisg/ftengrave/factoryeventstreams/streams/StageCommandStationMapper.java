package org.unisg.ftengrave.factoryeventstreams.streams;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.kafka.streams.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.unisg.ftengrave.factoryeventstreams.dto.ItemStationAssignment;

@Component
public class StageCommandStationMapper {

  private static final Logger LOGGER = LoggerFactory.getLogger(StageCommandStationMapper.class);

  private static final String RUN_ITEM_INTAKE_COMMAND = "run-item-intake-command";
  private static final String RUN_PRODUCTION_COMMAND = "run-production-command";
  private static final String RUN_ITEM_QC_COMMAND = "run-item-qc-command";

  private final ObjectMapper objectMapper;
  private final Set<String> intakeStationIdentifiers;
  private final Set<String> manufacturingStationIdentifiers;
  private final Set<String> ocStationIdentifiers;

  public StageCommandStationMapper(
      ObjectMapper objectMapper,
      @Value("${factory-streams.vacuum-gripper.station-identifiers:FTFactory/VGR_1,VGR_1,vgr_1,vacuum-gripper}")
      String intakeStationIdentifiers,
      @Value("${factory-streams.engraver.station-identifiers:FTFactory/OV_1,OV_1,oven,engraver}") String engraverStationIdentifiers,
      @Value("${factory-streams.polishing-machine.station-identifiers:FTFactory/MM_1,MM_1,mm_1,polishing-machine}")
      String polishingMachineStationIdentifiers,
      @Value("${factory-streams.workstation-transport.station-identifiers:FTFactory/WT_1,WT_1,wt_1,workstation-transport}")
      String workstationTransportStationIdentifiers,
      @Value("${factory-streams.qc.station-identifiers:FTFactory/SM_1,SM_1,sm_1,sorting-machine,sorter}")
      String ocStationIdentifiers) {
    this.objectMapper = objectMapper;
    this.intakeStationIdentifiers = identifiers(intakeStationIdentifiers);
    this.manufacturingStationIdentifiers = identifiers(
        engraverStationIdentifiers + "," + polishingMachineStationIdentifiers + ","
            + workstationTransportStationIdentifiers);
    this.ocStationIdentifiers = identifiers(ocStationIdentifiers);
  }

  public Optional<KeyValue<String, String>> toAssignment(String commandJson, long validFromTimestamp) {
    try {
      JsonNode root = objectMapper.readTree(commandJson);
      String commandType = text(root, "commandType").or(() -> text(root, "eventType")).orElse(null);
      String itemIdentifier = text(root, "itemIdentifier").orElse(null);
      Optional<OrchestrationStation> station = stationForCommand(commandType);

      if (itemIdentifier == null || itemIdentifier.isBlank() || station.isEmpty()) {
        LOGGER.debug("Ignoring stage command without item/station mapping: {}", commandJson);
        return Optional.empty();
      }

      String stationValue = station.get().value();
      String assignmentJson = objectMapper.writeValueAsString(
          new ItemStationAssignment(itemIdentifier, stationValue, validFromTimestamp));
      return Optional.of(KeyValue.pair(stationValue, assignmentJson));
    } catch (Exception exception) {
      LOGGER.warn("Ignoring malformed stage orchestration command", exception);
      return Optional.empty();
    }
  }

  public OrchestrationStation stationForRawEvent(String sourceTopic, JsonNode rawEvent) {
    String originalSourceTopic = text(rawEvent, RawFactoryEventEnricher.SOURCE_TOPIC_FIELD).orElse(sourceTopic);
    String station = text(rawEvent, "station").orElse(originalSourceTopic);
    if (matches(intakeStationIdentifiers, station, originalSourceTopic)) {
      return OrchestrationStation.INTAKE;
    }
    if (matches(manufacturingStationIdentifiers, station, originalSourceTopic)) {
      return OrchestrationStation.MANUFACTURING;
    }
    if (matches(ocStationIdentifiers, station, originalSourceTopic)) {
      return OrchestrationStation.OC;
    }
    return OrchestrationStation.UNKNOWN;
  }

  private Optional<OrchestrationStation> stationForCommand(String commandType) {
    return switch (commandType) {
      case RUN_ITEM_INTAKE_COMMAND -> Optional.of(OrchestrationStation.INTAKE);
      case RUN_PRODUCTION_COMMAND -> Optional.of(OrchestrationStation.MANUFACTURING);
      case RUN_ITEM_QC_COMMAND -> Optional.of(OrchestrationStation.OC);
      case null, default -> Optional.empty();
    };
  }

  private boolean matches(Set<String> identifiers, String station, String sourceTopic) {
    return (station != null && identifiers.contains(station))
        || (sourceTopic != null && identifiers.contains(sourceTopic));
  }

  private Set<String> identifiers(String identifiers) {
    return Arrays.stream(identifiers.split(","))
        .map(String::trim)
        .filter(identifier -> !identifier.isBlank())
        .collect(Collectors.toUnmodifiableSet());
  }

  private Optional<String> text(JsonNode root, String field) {
    return root.hasNonNull(field) ? Optional.of(root.get(field).asText()) : Optional.empty();
  }
}
