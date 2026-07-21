package com.trading.security;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * 【職責】作為 TradingSpringSecurity 的啟動入口，建立 Spring 容器並啟用 JPA 審計。
 * 【技巧】以 {@code @SpringBootApplication} 組合自動設定與元件掃描，並以 {@code @EnableJpaAuditing} 開啟建立／更新時間填寫。
 * 【概念】啟動類只組裝框架能力；商業規則與安全政策應放在各層元件，避免混雜生命週期與測試邊界。
 * 【邊界】不處理 API 路由或 JWT 簽發細節。
 */
@SpringBootApplication
@EnableJpaAuditing
public class TradingSpringSecurityApplication {

    public static void main(String[] args) {
        SpringApplication.run(TradingSpringSecurityApplication.class, args);
    }
}
