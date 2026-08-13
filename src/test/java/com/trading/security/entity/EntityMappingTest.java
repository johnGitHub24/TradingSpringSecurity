package com.trading.security.entity;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Table;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 【職責】單元測試 JPA 映射表名與規格 schema 一致。
 * 【技巧】讀取 {@link Table}／{@link CollectionTable} 註解，不啟動 DataSource。
 * 【概念】與 DB-001 整合層同一 SQL 契約：users／user_roles／orders 必須存在。
 */
class EntityMappingTest {

    /**
     * CASE DB-001：Entity 宣告的表名為 users、user_roles、orders。
     * Given: UserEntity／OrderEntity 註解；When: 讀取 name；Then: 與 INFORMATION_SCHEMA 驗證目標相同。
     */
    @Test
    void DB_001_entityAnnotations_declareUsersRolesAndOrdersTables() throws Exception {
        assertThat(UserEntity.class.getAnnotation(Table.class).name()).isEqualTo("users");
        assertThat(OrderEntity.class.getAnnotation(Table.class).name()).isEqualTo("orders");

        Field roles = UserEntity.class.getDeclaredField("roles");
        CollectionTable collection = roles.getAnnotation(CollectionTable.class);
        assertThat(collection).isNotNull();
        assertThat(collection.name()).isEqualTo("user_roles");
    }
}
