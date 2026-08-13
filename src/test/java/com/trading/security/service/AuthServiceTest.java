package com.trading.security.service;

import com.trading.security.domain.Role;
import com.trading.security.dto.JwtResponse;
import com.trading.security.dto.LoginRequest;
import com.trading.security.dto.RegisterRequest;
import com.trading.security.security.JwtTokenProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 【職責】單元測試 {@link AuthService}：註冊委派、登入簽發 JWT、錯誤帳密向上拋出。
 * 【技巧】Mock AuthenticationManager／JwtTokenProvider／UserService，不啟動 Filter Chain。
 * 【概念】與 AUTH-001, AUTH-002, USER-001 整合層同一契約：成功有 Bearer Token；失敗是 BadCredentials；註冊固定 USER 角色。
 */
@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    AuthenticationManager authenticationManager;
    @Mock
    JwtTokenProvider tokenProvider;
    @Mock
    UserService userService;

    @InjectMocks
    AuthService authService;

    /**
     * CASE USER-001：註冊委派 UserService 並固定 ROLE USER。
     * Given: 合法 RegisterRequest；When: register；Then: 以 USER 角色委派，不自行指定 ADMIN。
     */
    @Test
    void USER_001_register_delegatesToUserServiceWithUserRole() {
        RegisterRequest request = new RegisterRequest("alice", "secret123");

        authService.register(request);

        verify(userService).register("alice", "secret123", Set.of(Role.USER));
    }

    /**
     * CASE AUTH-001 / JWT-001：合法認證後回傳 Bearer Token、帳號與角色。
     * Given: AuthenticationManager 回傳 ROLE_USER；When: login；Then: tokenType=Bearer 且含 username／roles。
     */
    @Test
    void AUTH_001_login_authenticatesAndReturnsBearerToken() {
        LoginRequest request = new LoginRequest("alice", "secret123");
        var auth = new UsernamePasswordAuthenticationToken(
                "alice", "secret123", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(tokenProvider.generateToken(eq("alice"), any())).thenReturn("jwt-token");

        JwtResponse response = authService.login(request);

        assertThat(response.token()).isEqualTo("jwt-token");
        assertThat(response.tokenType()).isEqualTo("Bearer");
        assertThat(response.username()).isEqualTo("alice");
        assertThat(response.roles()).contains("ROLE_USER");
    }

    /**
     * CASE AUTH-002：帳密錯誤時 BadCredentialsException 不在 Service 被吞掉。
     * Given: authenticate 拋 BadCredentialsException；When: login；Then: 同一例外向上傳（整合層對應 401）。
     */
    @Test
    void AUTH_002_badCredentials_propagates() {
        LoginRequest request = new LoginRequest("alice", "wrong-pass");
        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("bad"));

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BadCredentialsException.class);
    }
}
