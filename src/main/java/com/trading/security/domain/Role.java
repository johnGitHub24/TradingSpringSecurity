package com.trading.security.domain;

/**
 * 【職責】定義應用程式可授予使用者的角色。
 * 【技巧】由 {@link com.trading.security.service.UserService} 轉換為 Spring Security 的 {@code ROLE_} 權限字串。
 * 【概念】角色是粗粒度授權模型；集中列舉值可避免各端點使用不一致的權限文字。
 * 【邊界】不決定特定 URL 或操作的授權政策。
 */
public enum Role {
    USER,
    ADMIN
}
