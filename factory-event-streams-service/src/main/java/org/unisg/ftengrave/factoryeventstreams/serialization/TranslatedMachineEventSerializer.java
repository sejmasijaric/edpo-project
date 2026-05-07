package org.unisg.ftengrave.factoryeventstreams.serialization;

import java.nio.charset.StandardCharsets;
import org.apache.kafka.common.serialization.Serializer;
import org.unisg.ftengrave.factoryeventstreams.dto.TranslatedMachineEvent;

public class TranslatedMachineEventSerializer implements Serializer<TranslatedMachineEvent> {

  @Override
  public byte[] serialize(String topic, TranslatedMachineEvent data) {
    if (data == null || data.payloadJson() == null) {
      return null;
    }
    return data.payloadJson().getBytes(StandardCharsets.UTF_8);
  }
}
