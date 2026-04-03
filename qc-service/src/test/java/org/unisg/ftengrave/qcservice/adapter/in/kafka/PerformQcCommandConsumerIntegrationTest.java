package org.unisg.ftengrave.qcservice.adapter.in.kafka;

import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.unisg.ftengrave.qcservice.DuplicateBusinessKeyException;
import org.unisg.ftengrave.qcservice.config.CamundaBusinessKeyConstraintInitializer;
import org.unisg.ftengrave.qcservice.adapter.in.kafka.dto.PerformQcCommandDto;
import org.unisg.ftengrave.qcservice.domain.ItemColor;
import org.unisg.ftengrave.qcservice.port.out.RequestColorDetectionPort;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:qc-service-business-key-test;DB_CLOSE_DELAY=-1",
        "spring.kafka.listener.auto-startup=false",
        "camunda.bpm.generate-unique-process-engine-name=true"
})
class PerformQcCommandConsumerIntegrationTest {

    @MockitoBean
    private RequestColorDetectionPort requestColorDetectionPort;

    @Autowired
    private PerformQcCommandConsumer performQcCommandConsumer;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RuntimeService runtimeService;

    @Test
    void consumeStartsQcAndRejectsDuplicateBusinessKeyForRunningProcessInstances() {
        performQcCommandConsumer.consume(new PerformQcCommandDto(
                "item-42",
                ItemColor.RED));
        assertThrows(DuplicateBusinessKeyException.class, () -> performQcCommandConsumer.consume(new PerformQcCommandDto(
                "item-42",
                ItemColor.RED)));

        Integer runningInstances = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM ACT_RU_EXECUTION WHERE PROC_DEF_ID_ IS NOT NULL AND BUSINESS_KEY_ = ?",
                Integer.class,
                "item-42"
        );

        assertThat(runningInstances).isEqualTo(1);
    }

    @Test
    void consumeStoresTargetColorAsProcessVariable() {
        performQcCommandConsumer.consume(new PerformQcCommandDto(
                "item-77",
                ItemColor.BLUE));

        Object targetColor = runtimeService.getVariable(
                runtimeService.createProcessInstanceQuery()
                        .processInstanceBusinessKey("item-77")
                        .singleResult()
                        .getId(),
                "targetColor"
        );

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
