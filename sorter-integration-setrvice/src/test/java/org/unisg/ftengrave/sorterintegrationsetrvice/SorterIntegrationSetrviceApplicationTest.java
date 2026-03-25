package org.unisg.ftengrave.sorterintegrationsetrvice;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
    "kafka.topic.auto-create=false",
    "spring.kafka.listener.auto-startup=false"
})
class SorterIntegrationSetrviceApplicationTest {

    @Test
    void contextLoads() {
    }
}
