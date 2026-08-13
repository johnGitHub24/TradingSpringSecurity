package com.trading.security.integration;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 【職責】整合測試 H2 schema 含 users／user_roles／orders。
 * 【技巧】以 JdbcTemplate 查 INFORMATION_SCHEMA，驗證 JPA ddl-auto 結果。
 * 【概念】與 DB-001 單元層（Entity @Table 名稱）同一契約。
 */
@Tag("integration")
@SpringBootTest
@ActiveProfiles("test")
class DatabaseSchemaIntegrationTest {

    @Autowired
    JdbcTemplate jdbcTemplate;

    /**
     * CASE DB-001：PUBLIC schema 含 USERS、USER_ROLES、ORDERS。
     * Given: Spring Boot 已啟動並套用 Entity；When: 查 INFORMATION_SCHEMA；Then: 三表存在。
     */
    @Test
    void DB_001_schema_containsUsersRolesAndOrdersTables() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = 'PUBLIC'",
                String.class);
        assertThat(tables).contains("USERS", "USER_ROLES", "ORDERS");
    }
}
