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
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 單元測試：覆蓋 {@link com.trading.security.service.AuthService}。
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

    // AUTH-UNIT-001
    @Test
    void register_delegatesToUserServiceWithUserRole() {
        RegisterRequest request = new RegisterRequest("alice", "secret123");

        authService.register(request);

        verify(userService).register("alice", "secret123", Set.of(Role.USER));
    }

    // AUTH-UNIT-002
    @Test
    void login_authenticatesAndReturnsBearerToken() {
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
}
