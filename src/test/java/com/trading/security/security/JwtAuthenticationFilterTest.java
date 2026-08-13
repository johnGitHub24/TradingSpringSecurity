package com.trading.security.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 【職責】單元測試 {@link JwtAuthenticationFilter}：無 Token／無效 Token 不寫入 Context；合法 Bearer 寫入角色。
 * 【技巧】Mock Servlet 與 {@link JwtTokenProvider}，直接呼叫 {@code doFilterInternal}。
 * 【概念】與 SEC-002, JWT-002, SEC-006 整合層同一契約：沒有有效認證時後續授權會 401 或 403；USER 不含 ADMIN。
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    JwtTokenProvider tokenProvider;
    @Mock
    HttpServletRequest request;
    @Mock
    HttpServletResponse response;
    @Mock
    FilterChain filterChain;

    private JwtAuthenticationFilter filter;

    @BeforeEach
    void setUp() {
        filter = new JwtAuthenticationFilter(tokenProvider);
        SecurityContextHolder.clearContext();
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    /**
     * CASE SEC-002：沒有 Authorization Header 時不建立認證。
     * Given: Header 為 null；When: doFilterInternal；Then: Context 為空且繼續 FilterChain（整合層 401）。
     */
    @Test
    void SEC_002_doFilter_withoutAuthorizationHeader_leavesContextEmpty() throws Exception {
        when(request.getHeader("Authorization")).thenReturn(null);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
        verifyNoInteractions(tokenProvider);
    }

    /**
     * CASE JWT-002 / SEC-002：無效 Token 不寫入 SecurityContext。
     * Given: Bearer 但 validate=false；When: filter；Then: 認證仍為 null（整合層 401）。
     */
    @Test
    void JWT_002_doFilter_whenTokenInvalid_leavesContextEmpty() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer bad.token");
        when(tokenProvider.validateToken("bad.token")).thenReturn(false);

        filter.doFilterInternal(request, response, filterChain);

        assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
        verify(filterChain).doFilter(request, response);
    }

    /**
     * CASE JWT-001 / SEC-001：合法 Bearer 寫入 username 與 ROLE_USER。
     * Given: validate=true 且 roles=ROLE_USER；When: filter；Then: SecurityContext 有 alice／USER。
     */
    @Test
    void JWT_001_doFilter_whenValidBearer_setsAuthentication() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer good.token");
        when(tokenProvider.validateToken("good.token")).thenReturn(true);
        when(tokenProvider.getUsername("good.token")).thenReturn("alice");
        when(tokenProvider.getRoles("good.token")).thenReturn(List.of("ROLE_USER"));

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getName()).isEqualTo("alice");
        assertThat(auth.getAuthorities()).extracting("authority").containsExactly("ROLE_USER");
        verify(filterChain).doFilter(request, response);
    }

    /**
     * CASE SEC-006：USER Token 不含 ROLE_ADMIN（整合層非 ADMIN 刪除 403）。
     * Given: roles 僅 ROLE_USER；When: filter；Then: authorities 不含 ROLE_ADMIN。
     */
    @Test
    void SEC_006_doFilter_whenUserToken_doesNotGrantAdmin() throws Exception {
        when(request.getHeader("Authorization")).thenReturn("Bearer user.token");
        when(tokenProvider.validateToken("user.token")).thenReturn(true);
        when(tokenProvider.getUsername("user.token")).thenReturn("bob");
        when(tokenProvider.getRoles("user.token")).thenReturn(List.of("ROLE_USER"));

        filter.doFilterInternal(request, response, filterChain);

        var auth = SecurityContextHolder.getContext().getAuthentication();
        assertThat(auth).isNotNull();
        assertThat(auth.getAuthorities()).extracting("authority")
                .contains("ROLE_USER")
                .doesNotContain("ROLE_ADMIN");
    }
}
