package org.unisg.ftengrave.intakeservice.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class CamundaBusinessKeyConstraintInitializer {

    public static final String BUSINESS_KEY_INDEX_NAME = "ACT_UNIQ_RU_BUS_KEY";

    private static final String CREATE_BUSINESS_KEY_INDEX_SQL = """
            CREATE UNIQUE INDEX IF NOT EXISTS ACT_UNIQ_RU_BUS_KEY
            ON ACT_RU_EXECUTION (PROC_DEF_ID_, BUSINESS_KEY_)
            """;

    private final JdbcTemplate jdbcTemplate;

    @EventListener(ApplicationReadyEvent.class)
    public void ensureUniqueRuntimeBusinessKeyConstraint() {
        jdbcTemplate.execute(CREATE_BUSINESS_KEY_INDEX_SQL);
        log.info("Ensured Camunda runtime business key index {} exists", BUSINESS_KEY_INDEX_NAME);
    }
}
