package org.unisg.ftengrave.qcservice;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.unisg.ftengrave.qcservice.adapter.out.kafka.RequestColorDetectionPublisher;
import org.unisg.ftengrave.qcservice.config.CamundaBusinessKeyConstraintInitializer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:qc-service-business-key-test;DB_CLOSE_DELAY=-1")
@AutoConfigureMockMvc
class TemporaryStartQcControllerIntegrationTest {

    @MockitoBean
    private RequestColorDetectionPublisher requestColorDetectionPublisher;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void startQcRejectsDuplicateBusinessKeyForRunningProcessInstances() throws Exception {
        mockMvc.perform(post("/temporary/start-qc/item-42"))
                .andExpect(status().isAccepted());

        mockMvc.perform(post("/temporary/start-qc/item-42"))
                .andExpect(status().isConflict());

        Integer runningInstances = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ACT_RU_EXECUTION WHERE PROC_DEF_ID_ IS NOT NULL AND BUSINESS_KEY_ = ?",
                Integer.class,
                "item-42"
        );

        assertThat(runningInstances).isEqualTo(1);
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
