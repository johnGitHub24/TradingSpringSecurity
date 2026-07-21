package com.trading.security.config;

import com.trading.security.security.JwtAuthenticationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.http.HttpStatus;

/**
 * 【職責】組裝無狀態 JWT 安全過濾鏈、公開路徑、角色授權與密碼編碼相關 Bean。
 * 【技巧】以 {@link SecurityFilterChain} DSL 關閉 CSRF／Session，並在 UsernamePassword 過濾器前插入 JWT 過濾器。
 * 【概念】API 採 Token 認證時不需要伺服器 Session；授權規則集中於此可避免各 Controller 重複判斷。
 * 【邊界】不實作登入／註冊 API，也不負責 Token 簽發內容。
 */
@Configuration
public class SecurityConfig {

    private static final String[] PUBLIC_PATHS = {
            "/api/auth/**",
            "/v3/api-docs/**",
            "/swagger-ui/**",
            "/swagger-ui.html",
            "/actuator/health",
            "/actuator/info",
            "/h2-console/**"
    };

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    /**
     * 【職責】建立無狀態 Session 的 SecurityFilterChain，並掛上 JWT 過濾器。
     * 【技巧】公開路徑 permitAll、DELETE 訂單要求 ADMIN，其餘需認證；未認證回 401。
     * 【概念】過濾鏈是請求進入業務前的安全閘門；把規則寫在設定類比散落在方法上更易審查。
     * @param http 可組態的 HttpSecurity
     * @return 建置完成的過濾鏈
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .headers(headers -> headers.frameOptions(frame -> frame.disable()))
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(PUBLIC_PATHS).permitAll()
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/orders/**").hasRole("ADMIN")
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /**
     * 【職責】提供註冊與驗證共用的 BCrypt 密碼編碼器。
     * 【技巧】以 {@link BCryptPasswordEncoder} 作為 {@link PasswordEncoder} Bean 注入。
     * 【概念】雜湊演算法應全系統一致；集中宣告可避免註冊與登入使用不同編碼器。
     * @return 密碼編碼器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 【職責】暴露 AuthenticationManager 供登入服務驗證帳密。
     * 【技巧】自 {@link AuthenticationConfiguration} 取得框架組裝好的管理器。
     * 【概念】在新版 Security 中需明確暴露此 Bean，服務層才能注入並呼叫 authenticate。
     * @param config 認證設定來源
     * @return AuthenticationManager
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }
}
