package org.unisg.ftengrave.orderorchestrator;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
        "camunda.bpm.generate-unique-process-engine-name=true",
        "camunda.bpm.generate-unique-process-application-name=true"
})
class OrderOrchestratorApplicationTest {

    @Test
    void contextLoads() {
    }
}
