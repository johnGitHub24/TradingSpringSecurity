package com.trading.security.config;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.ConfigurableEnvironment;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

/**
 * {@link StartupInfoLogger} 單元測試。
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("StartupInfoLogger 單元測試")
class StartupInfoLoggerTest {

    @Mock
    private ApplicationReadyEvent event;

    @Mock
    private ConfigurableApplicationContext applicationContext;

    @Mock
    private ConfigurableEnvironment env;

    private final StartupInfoLogger logger = new StartupInfoLogger();

    @Test
    @DisplayName("enabled=false → 不印任何內容")
    void disabled_printsNothing() {
        when(event.getApplicationContext()).thenReturn(applicationContext);
        when(applicationContext.getEnvironment()).thenReturn(env);
        when(env.getProperty("startup.info.enabled", Boolean.class, true)).thenReturn(false);

        String out = captureStdout(() -> logger.onApplicationEvent(event));

        assertThat(out).doesNotContain("後端已啟動");
    }

    @Test
    @DisplayName("api-only + h2 + swagger → 印出 health、H2、Swagger、extra-paths")
    void apiOnly_printsHealthH2SwaggerAndExtraPaths() {
        when(event.getApplicationContext()).thenReturn(applicationContext);
        when(applicationContext.getEnvironment()).thenReturn(env);
        when(env.getProperty("startup.info.enabled", Boolean.class, true)).thenReturn(true);
        when(env.getProperty("startup.info.project-name", "TradingSpringSecurity"))
                .thenReturn("TradingSpringSecurity");
        when(env.getProperty("server.port", "8080")).thenReturn("8080");
        when(env.getProperty("startup.info.frontend", "none")).thenReturn("none");
        when(env.getProperty("startup.info.auth", Boolean.class, false)).thenReturn(true);
        when(env.getProperty("startup.info.h2", Boolean.class, true)).thenReturn(true);
        when(env.getProperty("startup.info.api-docs", Boolean.class, true)).thenReturn(true);
        when(env.getProperty("spring.datasource.url", "jdbc:h2:mem:tradingdb"))
                .thenReturn("jdbc:h2:mem:tradingdb");
        when(env.getProperty("startup.info.extra-paths[0]")).thenReturn(null);
        when(env.getProperty("startup.info.extra-paths"))
                .thenReturn("/api/auth/login,/api/v1/orders");

        String out = captureStdout(() -> logger.onApplicationEvent(event));

        assertThat(out).contains("TradingSpringSecurity 後端已啟動");
        assertThat(out).contains("http://localhost:8080/actuator/health");
        assertThat(out).contains("Swagger UI");
        assertThat(out).contains("H2 Console");
        assertThat(out).contains("http://localhost:8080/api/auth/login");
        assertThat(out).contains("http://localhost:8080/api/v1/orders");
    }

    private static String captureStdout(Runnable action) {
        PrintStream original = System.out;
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        try (PrintStream ps = new PrintStream(buffer, true, StandardCharsets.UTF_8)) {
            System.setOut(ps);
            action.run();
        } finally {
            System.setOut(original);
        }
        return buffer.toString(StandardCharsets.UTF_8);
    }
}
