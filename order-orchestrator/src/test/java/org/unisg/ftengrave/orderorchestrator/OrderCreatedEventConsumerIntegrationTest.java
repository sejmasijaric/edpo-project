package org.unisg.ftengrave.orderorchestrator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.unisg.ftengrave.orderorchestrator.adapter.in.kafka.OrderCreatedEventConsumer;
import org.unisg.ftengrave.orderorchestrator.adapter.in.kafka.dto.OrderCreatedEventDto;
import org.unisg.ftengrave.orderorchestrator.config.CamundaBusinessKeyConstraintInitializer;
import org.unisg.ftengrave.orderorchestrator.domain.ItemColor;
import org.unisg.ftengrave.orderorchestrator.port.out.SendRunIntakeCommandPort;

@SpringBootTest(properties = {
        "spring.datasource.url=jdbc:h2:mem:order-orchestrator-business-key-test;DB_CLOSE_DELAY=-1",
        "kafka.topic.auto-create=false",
        "spring.kafka.listener.auto-startup=false",
        "camunda.bpm.generate-unique-process-engine-name=true",
        "camunda.bpm.generate-unique-process-application-name=true"
})
class OrderCreatedEventConsumerIntegrationTest {

    @MockitoBean
    private SendRunIntakeCommandPort sendRunIntakeCommandPort;

    @Autowired
    private OrderCreatedEventConsumer orderCreatedEventConsumer;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private HistoryService historyService;

    @Autowired
    private RuntimeService runtimeService;

    @Test
    void consumeStartsProcessAndSendsRunIntakeCommand() {
        orderCreatedEventConsumer.consume(new OrderCreatedEventDto("item-42", "red"));

        verify(sendRunIntakeCommandPort).publish("item-42", ItemColor.RED);
        assertThat(runtimeService.createProcessInstanceQuery()
                .processInstanceBusinessKey("item-42")
                .singleResult()).isNotNull();
    }

    @Test
    void consumeStoresTargetColorAsHistoricProcessVariable() {
        orderCreatedEventConsumer.consume(new OrderCreatedEventDto("item-77", "blue"));

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
