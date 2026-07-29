package com.trading.security.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 【職責】應用就緒後於 Console 印出常用 URL（health／Swagger／H2／Auth／Orders），方便 IntelliJ 本機啟動。
 * 【技巧】聽 {@link ApplicationReadyEvent}；開關全來自 {@code startup.info.*}；以 UTF-8 {@link PrintStream} 寫出；需 JVM {@code -Dstdout.encoding=UTF-8} 與 IDE Console=UTF-8（見 EOS knowledge）。
 * 【概念】開發便利輸出，不是業務邏輯；用 YAML 開關適配 static／vite／api-only，避免改 Java。
 * 【邊界】不負責前端啟動、不驗證 URL 是否可連。
 */
@Component
public class StartupInfoLogger implements ApplicationListener<ApplicationReadyEvent> {

    private static final Logger log = LoggerFactory.getLogger(StartupInfoLogger.class);

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        Environment env = event.getApplicationContext().getEnvironment();
        if (!env.getProperty("startup.info.enabled", Boolean.class, true)) {
            return;
        }

        String project = env.getProperty("startup.info.project-name", "TradingSpringSecurity");
        String port = env.getProperty("server.port", "8080");
        String base = "http://localhost:" + port;
        String frontend = env.getProperty("startup.info.frontend", "none");
        boolean auth = env.getProperty("startup.info.auth", Boolean.class, false);
        boolean h2 = env.getProperty("startup.info.h2", Boolean.class, true);
        boolean apiDocs = env.getProperty("startup.info.api-docs", Boolean.class, true);

        PrintStream out = utf8Out();
        out.println();
        out.println("╔════════════════════════════════════════════════════════════════════════╗");
        out.printf("║  %-70s║%n", project + " 後端已啟動 — 使用連結");
        out.println("╠════════════════════════════════════════════════════════════════════════╣");
        out.println("║ 【後端 API / 工具】                                                      ║");
        out.printf("║   健康檢查     %s%n", base + "/actuator/health");
        out.printf("║   應用資訊     %s%n", base + "/actuator/info");
        if (apiDocs) {
            out.printf("║   Swagger UI   %s%n", base + "/swagger-ui.html");
            out.printf("║   OpenAPI JSON %s%n", base + "/v3/api-docs");
        }
        if (h2) {
            out.printf("║   H2 Console   %s%n", base + "/h2-console");
            String jdbc = env.getProperty("spring.datasource.url", "jdbc:h2:mem:tradingdb");
            out.printf("║   H2 JDBC URL  %s  帳號 sa  密碼 (空白)%n", jdbc);
        }

        if (!"none".equalsIgnoreCase(frontend)) {
            out.println("╠════════════════════════════════════════════════════════════════════════╣");
            if ("static".equalsIgnoreCase(frontend)) {
                out.println("║ 【前台】同埠靜態資源                                                      ║");
                out.printf("║   首頁         %s%n", base + env.getProperty("startup.info.home-path", "/"));
                for (String path : extraPaths(env)) {
                    out.printf("║   額外         %s%n", base + path);
                }
            } else if ("vite".equalsIgnoreCase(frontend)) {
                String feBase = "http://localhost:" + env.getProperty("startup.info.frontend-port", "5173");
                out.println("║ 【前台 Vue】需另執行 Frontend (Vite) 或 Full Stack                         ║");
                if (auth) {
                    out.printf("║   登入頁       %s%n", feBase + env.getProperty("startup.info.login-path", "/login"));
                }
                out.printf("║   主頁         %s%n", feBase + env.getProperty("startup.info.home-path", "/orders"));
            }
            if (auth) {
                out.printf("║   預設帳號     %s / %s%n",
                        env.getProperty("startup.info.default-user", "admin"),
                        env.getProperty("startup.info.default-pass", "admin123"));
            }
        }

        // API-only：以 extra-paths 印出 Auth／Orders 等（無需「前台」區塊）
        if ("none".equalsIgnoreCase(frontend)) {
            List<String> extras = extraPaths(env);
            if (!extras.isEmpty()) {
                for (String path : extras) {
                    out.printf("║   額外         %s%n", base + path);
                }
            }
        }

        out.println("╚════════════════════════════════════════════════════════════════════════╝");
        out.println();
        log.info("{} ready — frontend={} | {}", project, frontend, base + "/actuator/health");
    }


    /**
     * 【職責】以 UTF-8 寫出 banner（與 JVM stdout.encoding=UTF-8、IDE Console UTF-8 對齊）。
     * 【技巧】勿依賴系統預設 MS950；端到端 UTF-8 才能 run-anywhere。
     */
    private static PrintStream utf8Out() {
        return new PrintStream(System.out, true, StandardCharsets.UTF_8);
    }

    private static List<String> extraPaths(Environment env) {
        String first = env.getProperty("startup.info.extra-paths[0]");
        if (first != null && !first.isBlank()) {
            List<String> paths = new ArrayList<>();
            for (int i = 0; ; i++) {
                String p = env.getProperty("startup.info.extra-paths[" + i + "]");
                if (p == null || p.isBlank()) {
                    break;
                }
                paths.add(p.startsWith("/") ? p : "/" + p);
            }
            return paths;
        }
        String raw = env.getProperty("startup.info.extra-paths");
        if (raw == null || raw.isBlank()) {
            return List.of();
        }
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(s -> s.startsWith("/") ? s : "/" + s)
                .toList();
    }
}
