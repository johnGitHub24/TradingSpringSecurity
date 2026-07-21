package com.trading.security.dto;

import jakarta.validation.constraints.NotBlank;

/**
 * 【職責】承載登入端點所需的帳號與密碼。
 * 【技巧】以 record 及 {@code @NotBlank} 建立不可變且可由 Spring 自動驗證的請求契約。
 * 【概念】請求 DTO 將外部輸入與安全服務隔離，避免 Controller 直接依賴持久化使用者模型。
 * 【邊界】不包含 Token、角色或已驗證身分。
 */
public record LoginRequest(
        @NotBlank(message = "username 不可為空")
        String username,

        @NotBlank(message = "password 不可為空")
        String password
) {
}
