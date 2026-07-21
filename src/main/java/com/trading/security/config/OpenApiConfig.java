package com.trading.security.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 【職責】定義 OpenAPI／Swagger UI 的文件標題、版本與 Bearer JWT 安全方案。
 * 【技巧】以 springdoc 的 {@link OpenAPI} Bean 宣告全域 {@code bearerAuth} SecurityScheme。
 * 【概念】集中文件中繼資料可讓 Swagger UI 支援 Authorize；個別端點描述仍由 Controller 註解提供。
 * 【邊界】不描述各 API 的請求／回應細節。
 */
@Configuration
public class OpenApiConfig {

    private static final String SCHEME_NAME = "bearerAuth";

    /**
     * 【職責】建立可供 Swagger UI 使用的 OpenAPI 定義。
     * 【技巧】在 Components 註冊 HTTP Bearer／JWT 方案，供受保護端點引用。
     * 【概念】文件即契約的一部分；把認證方式寫進 OpenAPI，可降低前後端對 Header 格式的誤解。
     * @return OpenAPI 文件物件
     */
    @Bean
    public OpenAPI tradingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("TradingSpringSecurity API")
                        .description("Spring Security JWT + Order API demo")
                        .version("0.1.0"))
                .components(new Components().addSecuritySchemes(SCHEME_NAME,
                        new SecurityScheme()
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
