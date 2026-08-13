package com.trading.security.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.trading.security.domain.Role;
import com.trading.security.entity.UserEntity;
import com.trading.security.repository.UserRepository;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;
import java.util.Set;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * 【職責】整合測試 JWT 保護下的訂單 API 與角色授權。
 * 【技巧】MockMvc + H2 + 真實 SecurityFilterChain；ADMIN 以 Repository 種子帳號登入。
 * 【概念】與 ORDER-001, SEC-001 等單元層同一契約：201、401、400、409、404、403、204。
 */
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class SecurityApiIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    private String json(Object o) throws Exception {
        return objectMapper.writeValueAsString(o);
    }

    private String registerAndLogin(String username, String password) throws Exception {
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json(Map.of("username", username, "password", password))));
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andReturn().getResponse().getContentAsString();
        JsonNode node = objectMapper.readTree(body);
        return node.get("token").asText();
    }

    private String loginExisting(String username, String password) throws Exception {
        String body = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("username", username, "password", password))))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString();
        return objectMapper.readTree(body).get("token").asText();
    }

    private void seedAdmin(String username, String password) {
        userRepository.save(UserEntity.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles(Set.of(Role.USER, Role.ADMIN))
                .build());
    }

    private Map<String, Object> order(String clientOrderId) {
        return Map.of("clientOrderId", clientOrderId, "symbol", "BTCUSDT",
                "side", "BUY", "quantity", 0.5, "price", 65000);
    }

    /**
     * CASE SEC-001 / ORDER-001 / JWT-001：已認證建單 201、status=NEW。
     * Given: 合法 JWT；When: POST /api/v1/orders；Then: 201 且 NEW。
     */
    @Test
    void SEC_001_createOrder_authenticated_returns201() throws Exception {
        String token = registerAndLogin("sec001", "secret123");

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(order("ord-sec-001"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNumber())
                .andExpect(jsonPath("$.status").value("NEW"));
    }

    /**
     * CASE SEC-002：無 token 建單 401。
     * Given: 未帶 Authorization；When: POST /api/v1/orders；Then: 401。
     */
    @Test
    void SEC_002_createOrder_withoutToken_returns401() throws Exception {
        mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(order("ord-sec-002"))))
                .andExpect(status().isUnauthorized());
    }

    /**
     * CASE JWT-002：竄改 Token 存取受保護端點 401。
     * Given: 合法 Token 尾端被改；When: POST /api/v1/orders；Then: 401。
     */
    @Test
    void JWT_002_createOrder_withTamperedToken_returns401() throws Exception {
        String token = registerAndLogin("jwt002", "secret123");
        String tampered = token.substring(0, token.length() - 2) + "xx";

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + tampered)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(order("ord-jwt-002"))))
                .andExpect(status().isUnauthorized());
    }

    /**
     * CASE SEC-003：驗證失敗 400 VALIDATION_FAILED。
     * Given: 空 clientOrderId 與非法數量；When: 建單；Then: 400。
     */
    @Test
    void SEC_003_createOrder_withInvalidBody_returns400() throws Exception {
        String token = registerAndLogin("sec003", "secret123");

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(Map.of("clientOrderId", "", "symbol", "BTCUSDT",
                                "side", "BUY", "quantity", -1, "price", 0))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_FAILED"));
    }

    /**
     * CASE SEC-004 / ORDER-002：冪等鍵重複 409 DUPLICATE_ORDER。
     * Given: 同一 clientOrderId 已建單；When: 再 POST；Then: 409。
     */
    @Test
    void SEC_004_createOrder_duplicateClientOrderId_returns409() throws Exception {
        String token = registerAndLogin("sec004", "secret123");

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(order("dup-sec-004"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(order("dup-sec-004"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errorCode").value("DUPLICATE_ORDER"));
    }

    /**
     * CASE ORDER-003：查無訂單 404 ORDER_NOT_FOUND。
     * Given: 已認證、不存在的 id；When: GET /api/v1/orders/{id}；Then: 404。
     */
    @Test
    void ORDER_003_getOrder_whenMissing_returns404() throws Exception {
        String token = registerAndLogin("ord003", "secret123");

        mockMvc.perform(get("/api/v1/orders/99999")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errorCode").value("ORDER_NOT_FOUND"));
    }

    /**
     * CASE ORDER-005：已認證列表 200 且含剛建立的訂單。
     * Given: 已建一筆；When: GET /api/v1/orders；Then: 200 且 content 非空。
     */
    @Test
    void ORDER_005_listOrders_authenticated_returns200() throws Exception {
        String token = registerAndLogin("ord005", "secret123");
        mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(order("ord-list-005"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/orders")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.content[*].clientOrderId").value(
                        org.hamcrest.Matchers.hasItem("ord-list-005")));
    }

    /**
     * CASE SEC-005 / ORDER-004：取消 → CANCELLED。
     * Given: 已建 NEW 訂單；When: PATCH …/cancel；Then: 200 且 CANCELLED。
     */
    @Test
    void SEC_005_cancelOrder_setsStatusCancelled() throws Exception {
        String token = registerAndLogin("sec005", "secret123");

        String body = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(order("ord-sec-005"))))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(patch("/api/v1/orders/" + id + "/cancel")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    /**
     * CASE SEC-006：非 ADMIN 刪除 403。
     * Given: USER JWT 與自己的訂單；When: DELETE；Then: 403。
     */
    @Test
    void SEC_006_deleteOrder_asNonAdmin_returns403() throws Exception {
        String token = registerAndLogin("sec006", "secret123");

        String body = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(order("ord-sec-006"))))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(delete("/api/v1/orders/" + id)
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }

    /**
     * CASE SEC-007 / JWT-003：ADMIN 刪除 204。
     * Given: ADMIN Token 與 USER 建立的訂單；When: DELETE；Then: 204。
     */
    @Test
    void SEC_007_deleteOrder_asAdmin_returns204() throws Exception {
        seedAdmin("admin007", "secret123");
        String adminToken = loginExisting("admin007", "secret123");
        String userToken = registerAndLogin("sec007user", "secret123");

        String body = mockMvc.perform(post("/api/v1/orders")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json(order("ord-sec-007"))))
                .andReturn().getResponse().getContentAsString();
        long id = objectMapper.readTree(body).get("id").asLong();

        mockMvc.perform(delete("/api/v1/orders/" + id)
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());
    }
}
