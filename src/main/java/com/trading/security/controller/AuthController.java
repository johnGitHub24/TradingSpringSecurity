package com.trading.security.controller;

import com.trading.security.dto.JwtResponse;
import com.trading.security.dto.LoginRequest;
import com.trading.security.dto.RegisterRequest;
import com.trading.security.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 【職責】提供註冊與登入的 HTTP API 入口，將已驗證的請求交給 {@link AuthService}，並組裝 REST 回應。
 * 【技巧】以 Spring MVC 的 {@code @RestController}、{@code @RequestBody} 與 {@code @Valid} 將 JSON 轉為 DTO 並先執行 Bean Validation。
 * 【概念】Controller 是傳輸層的轉接器；它處理 HTTP 細節，而將密碼處理、認證與 Token 簽發留在服務與安全元件，
 *         可避免商業規則分散於端點實作，並讓同一服務能被其他入口重用。
 * 【邊界】不負責密碼雜湊、JWT 簽章、帳號持久化或決定認證規則。
 */
@Tag(name = "Auth", description = "註冊與登入（JWT）")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    /**
     * 【職責】接受新帳號資料並建立使用者，成功時回傳 {@code 201 Created}。
     * 【技巧】{@code @Valid} 在呼叫服務前套用 DTO 宣告的欄位規則；以 {@link ResponseEntity} 明確表達 HTTP 狀態。
     * 【概念】輸入格式驗證應位於 API 邊界，服務則專注於帳號唯一性與密碼處理等業務規則，兩者失敗時可由全域例外處理器統一回應。
     * 【邊界】不自行判定帳號是否存在，也不直接儲存使用者。
     *
     * @param request 註冊請求的帳號與明文密碼
     * @return 無回應本文的 {@code 201 Created}
     */
    @Operation(summary = "註冊新使用者")
    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody RegisterRequest request) {
        authService.register(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    /**
     * 【職責】接受登入憑證並回傳可用於後續受保護 API 的 Bearer JWT。
     * 【技巧】委派 {@link AuthService} 使用 Spring Security 的認證流程，再以 {@link ResponseEntity} 序列化 JWT 回應 DTO。
     * 【概念】端點本身不比對密碼；統一透過 AuthenticationManager 可沿用密碼編碼器、使用者載入與失敗處理，避免自行實作認證流程。
     * 【邊界】不解析 JWT、不將登入狀態存入 HTTP Session。
     *
     * @param request 含帳號與密碼的登入請求
     * @return 含 Token 類型、Token、帳號與角色的成功回應
     */
    @Operation(summary = "登入取得 JWT")
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
