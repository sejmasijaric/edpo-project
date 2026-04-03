package org.unisg.ftengrave.orderorchestrator;

import org.camunda.bpm.engine.HistoryService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.unisg.ftengrave.orderorchestrator.config.CamundaBusinessKeyConstraintInitializer;
import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;
import org.unisg.ftengrave.orderorchestrator.port.out.SendPerformQcCommandPort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:order-orchestrator-business-key-test;DB_CLOSE_DELAY=-1",
        "kafka.topic.auto-create=false",
        "camunda.bpm.generate-unique-process-engine-name=true",
        "camunda.bpm.generate-unique-process-application-name=true"
})
@AutoConfigureMockMvc
class TemporaryOrderCreatedControllerIntegrationTest {

    @MockitoBean
    private SendPerformQcCommandPort sendPerformQcCommandPort;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HistoryService historyService;

    @Test
    void orderCreatedStartsProcessAndSendsPerformQcCommand() throws Exception {
        mockMvc.perform(post("/temporary/order-created/item-42").param("targetColor", "RED"))
                .andExpect(status().isAccepted());

        verify(sendPerformQcCommandPort).publish("item-42", ItemColor.RED);
    }

    @Test
    void orderCreatedStoresTargetColorAsHistoricProcessVariable() throws Exception {
        mockMvc.perform(post("/temporary/order-created/item-77").param("targetColor", "BLUE"))
                .andExpect(status().isAccepted());

        String processInstanceId = historyService.createHistoricProcessInstanceQuery()
                .processInstanceBusinessKey("item-77")
                .singleResult()
                .getId();

        String targetColor = historyService.createHistoricVariableInstanceQuery()
                .processInstanceId(processInstanceId)
                .variableName("targetColor")
                .singleResult()
                .getValue()
                .toString();

        assertThat(targetColor).isEqualTo(ItemColor.BLUE.name());
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
