package org.unisg.ftengrave.factoryeventstreams.serialization;

import org.apache.kafka.common.serialization.Deserializer;
import org.unisg.ftengrave.factoryeventstreams.dto.TranslatedMachineEvent;

public class TranslatedMachineEventDeserializer implements Deserializer<TranslatedMachineEvent> {

  @Override
  public TranslatedMachineEvent deserialize(String topic, byte[] data) {
    throw new UnsupportedOperationException("TranslatedMachineEvent is only serialized to output topics");
  }
}
