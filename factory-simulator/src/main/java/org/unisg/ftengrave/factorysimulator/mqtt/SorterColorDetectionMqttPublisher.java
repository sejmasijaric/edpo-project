package org.unisg.ftengrave.factorysimulator.mqtt;

import org.unisg.ftengrave.factorysimulator.domain.Item;

public interface SorterColorDetectionMqttPublisher {

  void publishDetectedColor(Item item);
}
