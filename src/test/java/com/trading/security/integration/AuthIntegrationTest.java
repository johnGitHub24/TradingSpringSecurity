package com.trading.security.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.security.support.SecurityTestFixtures;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 【職責】整合測試註冊／登入 API（H2 + Security Filter）。
 * 【技巧】MockMvc 打真實 DispatcherServlet；fixture 自 docs/test-data 載入。
 * 【概念】與 AUTH-001, AUTH-002, AUTH-003, USER-001 單元層同一契約。
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    /**
     * CASE USER-001：合法註冊回 201。
     * Given: 未使用的 username；When: POST /api/auth/register；Then: 201。
     */
    @Test
    void USER_001_register_withValidBody_returns201() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "user001", "password", "secret123"))))
                .andExpect(status().isCreated());
    }

    /**
     * CASE AUTH-001 / JWT-001：登入成功有 token。
     * Given: 已註冊帳號；When: POST /api/auth/login；Then: 200 且 token／username 有值。
     */
    @Test
    void AUTH_001_login_withValidCredentials_returnsToken() throws Exception {
        String payload = SecurityTestFixtures.loadJsonString("auth/AUTH-001-SUCCESS.json");
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty())
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.username").value("auth001"));
    }

    /**
     * CASE AUTH-002：錯誤密碼 401 INVALID_CREDENTIALS。
     * Given: 已註冊帳號、錯誤密碼；When: login；Then: 401。
     */
    @Test
    void AUTH_002_login_withWrongPassword_returns401() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "auth002", "password", "secret123"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "auth002", "password", "wrong-pass"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.errorCode").value("INVALID_CREDENTIALS"));
    }

    /**
     * CASE AUTH-003：重複註冊 409 USERNAME_EXISTS。
     * Given: 同一 username 已註冊；When: 再次 register；Then: 409。
     */
    @Test
    void AUTH_003_register_duplicateUsername_returns409() throws Exception {
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "auth003", "password", "secret123"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", "auth003", "password", "other-pass"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("USERNAME_EXISTS"));
    }
}
