package org.unisg.ftengrave.factorysimulator.mqtt;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.stereotype.Component;

@Component
public class MqttTimestampFactory {

  private static final DateTimeFormatter FORMATTER =
      DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SS");

  public String createTimestamp() {
    return LocalDateTime.now().format(FORMATTER);
  }
}
