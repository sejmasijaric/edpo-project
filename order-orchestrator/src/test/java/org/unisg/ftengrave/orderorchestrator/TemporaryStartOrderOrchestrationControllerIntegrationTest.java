package org.unisg.ftengrave.orderorchestrator;

import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.unisg.ftengrave.orderorchestrator.config.CamundaBusinessKeyConstraintInitializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:order-orchestrator-business-key-test;DB_CLOSE_DELAY=-1",
        "camunda.bpm.generate-unique-process-engine-name=true",
        "camunda.bpm.generate-unique-process-application-name=true"
})
@AutoConfigureMockMvc
class TemporaryStartOrderOrchestrationControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RuntimeService runtimeService;

    @Test
    void startOrderOrchestrationRejectsDuplicateBusinessKeyForRunningProcessInstances() throws Exception {
        mockMvc.perform(post("/temporary/start-order-orchestration/order-42"))
                .andExpect(status().isAccepted());

        mockMvc.perform(post("/temporary/start-order-orchestration/order-42"))
                .andExpect(status().isConflict());

        Integer runningInstances = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ACT_RU_EXECUTION WHERE PROC_DEF_ID_ IS NOT NULL AND BUSINESS_KEY_ = ?",
                Integer.class,
                "order-42"
        );

        assertThat(runningInstances).isEqualTo(1);
    }

    @Test
    void startOrderOrchestrationStoresOrderIdentifierAsProcessVariable() throws Exception {
        mockMvc.perform(post("/temporary/start-order-orchestration/order-77"))
                .andExpect(status().isAccepted());

        Object orderIdentifier = runtimeService.getVariable(
                runtimeService.createProcessInstanceQuery()
                        .processInstanceBusinessKey("order-77")
                        .singleResult()
                        .getId(),
                "orderIdentifier"
        );

        assertThat(orderIdentifier).isEqualTo("order-77");
    }

    @Test
    void startupCreatesCamundaRuntimeBusinessKeyIndex() {
        Integer matchingIndexes = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM INFORMATION_SCHEMA.INDEXES WHERE INDEX_NAME = ?",
                Integer.class,
                CamundaBusinessKeyConstraintInitializer.BUSINESS_KEY_INDEX_NAME
        );

        assertThat(matchingIndexes).isEqualTo(1);
    }
}
