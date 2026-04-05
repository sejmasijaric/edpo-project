package org.unisg.ftengrave.orderorchestrator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:order-orchestrator-context-test;DB_CLOSE_DELAY=-1",
        "kafka.topic.auto-create=false",
        "spring.kafka.listener.auto-startup=false",
        "camunda.bpm.generate-unique-process-engine-name=true",
        "camunda.bpm.generate-unique-process-application-name=true"
})
class OrderOrchestratorApplicationTest {

    @Test
    void contextLoads() {
    }
}
