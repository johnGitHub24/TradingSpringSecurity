package com.trading.security.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 單元測試：覆蓋 {@link com.trading.security.security.JwtTokenProvider}。
 */
class JwtTokenProviderTest {

    private JwtTokenProvider provider;

    @BeforeEach
    void setUp() {
        provider = new JwtTokenProvider(
                "unit-test-secret-key-must-be-at-least-256-bits-long-padding-xxxxx",
                3600000L);
    }

    // JWT-UNIT-001
    @Test
    void generateThenValidate_returnsTrueAndExtractsUsername() {
        String token = provider.generateToken("alice", List.of("ROLE_USER"));

        assertThat(provider.validateToken(token)).isTrue();
        assertThat(provider.getUsername(token)).isEqualTo("alice");
    }

    // JWT-UNIT-002
    @Test
    void validateToken_whenTampered_returnsFalse() {
        String token = provider.generateToken("alice", List.of("ROLE_USER"));
        String tampered = token.substring(0, token.length() - 2) + "xx";

        assertThat(provider.validateToken(tampered)).isFalse();
    }

    // JWT-UNIT-003
    @Test
    void getRoles_roundTripsAuthorities() {
        String token = provider.generateToken("admin", List.of("ROLE_USER", "ROLE_ADMIN"));

        assertThat(provider.getRoles(token)).containsExactly("ROLE_USER", "ROLE_ADMIN");
    }
}

