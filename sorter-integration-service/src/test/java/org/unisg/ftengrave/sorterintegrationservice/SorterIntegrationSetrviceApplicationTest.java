package org.unisg.ftengrave.sorterintegrationservice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "kafka.topic.auto-create=false",
    "spring.kafka.listener.auto-startup=false",
    "mqtt.bridge.enabled=false"
})
class SorterIntegrationServiceApplicationTest {

    @Test
    void contextLoads() {
    }
}
