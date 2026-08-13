package com.trading.security.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 【職責】單元測試 {@link JwtTokenProvider}：簽發內容、竄改失效、roles round-trip。
 * 【技巧】手動 new Provider（固定 256-bit 密鑰），不啟動 Spring。
 * 【概念】與 JWT-001, JWT-002, JWT-003 整合層同一契約：合法 Token 可解析；竄改無效；角色可還原。
 */
class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(
                "unit-test-secret-key-must-be-at-least-256-bits-long-padding-xxxxx",
                3600000L);
    }

    /**
     * CASE JWT-001 / AUTH-001 / SEC-001：產生後可驗證並取出 username。
     * Given: generateToken(alice, ROLE_USER)；When: validate／getUsername；Then: true 且 alice。
     */
    @Test
    void JWT_001_generateThenValidate_returnsTrueAndExtractsUsername() {
        String token = provider.generateToken("alice", List.of("ROLE_USER"));

        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUsername(token)).isEqualTo("alice");
    }

    /**
     * CASE JWT-002 / SEC-002：竄改 Token 尾端 → validate=false（整合層對無效 Token 回 401）。
     * Given: 合法 Token 改最後兩字元；When: validateToken；Then: false。
     */
    @Test
    void JWT_002_validateToken_whenTampered_returnsFalse() {
        String token = provider.generateToken("alice", List.of("ROLE_USER"));
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(provider.validateToken(tampered)).isFalse();
    }

    /**
     * CASE JWT-003 / SEC-007：roles claim round-trip，供 ADMIN 授權使用。
     * Given: USER+ADMIN；When: getRoles；Then: 兩個角色原樣還原。
     */
    @Test
    void JWT_003_getRoles_roundTripsAuthorities() {
        String token = provider.generateToken("admin", List.of("ROLE_USER", "ROLE_ADMIN"));

        assertThat(provider.getRoles(token)).containsExactly("ROLE_USER", "ROLE_ADMIN");
    }
}
