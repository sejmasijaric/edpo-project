package org.unisg.ftengrave.factoryeventstreams;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "kafka.topic.auto-create=false",
    "spring.kafka.streams.auto-startup=false"
})
class FactoryEventStreamsApplicationTest {

  @Test
  void contextLoads() {
  }
}
