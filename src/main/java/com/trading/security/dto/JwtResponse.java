package com.trading.security.dto;

import java.util.List;

/**
 * 【職責】承載登入成功後回傳給用戶端的 JWT 與身分摘要。
 * 【技巧】以 record 固定 token、tokenType、username、roles 欄位，並提供 Bearer 工廠方法。
 * 【概念】回應 DTO 讓前端知道如何組 Authorization Header，同時避免直接暴露安全內部物件。
 * 【邊界】不驗證或刷新 Token。
 */
public record JwtResponse(
        String token,
        String tokenType,
        String username,
        List<String> roles
) {
    /**
     * 【職責】組裝 tokenType 為 Bearer 的登入成功回應。
     * 【技巧】以靜態工廠固定 {@code Bearer} 字串，避免呼叫端拼錯類型。
     * 【概念】集中約定 Token 類型可降低前後端對 Header 前綴的不一致。
     * @param token JWT 字串
     * @param username 已驗證帳號
     * @param roles 角色列表
     * @return JwtResponse
     */
    public static JwtResponse bearer(String token, String username, List<String> roles) {
        return new JwtResponse(token, "Bearer", username, roles);
    }
}
