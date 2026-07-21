package com.trading.security.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * 【職責】承載註冊帳號所需的輸入與基本格式限制。
 * 【技巧】使用 Bean Validation 在 HTTP 邊界檢查帳號及密碼長度，並以 record 保持資料不可變。
 * 【概念】格式規則與帳號唯一性規則分層處理：前者由 DTO 快速拒絕，後者由服務與資料庫保護。
 * 【邊界】不允許用戶端指定角色，也不保存雜湊後密碼。
 */
public record RegisterRequest(
        @NotBlank(message = "username 不可為空")
        @Size(min = 3, max = 100, message = "username 長度需 3~100")
        String username,

        @NotBlank(message = "password 不可為空")
        @Size(min = 6, max = 100, message = "password 長度至少 6")
        String password
) {
}
