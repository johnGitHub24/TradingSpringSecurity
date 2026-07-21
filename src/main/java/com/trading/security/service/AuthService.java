package com.trading.security.service;

import com.trading.security.domain.Role;
import com.trading.security.dto.JwtResponse;
import com.trading.security.dto.LoginRequest;
import com.trading.security.dto.RegisterRequest;
import com.trading.security.security.JwtTokenProvider;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

/**
 * 【職責】協調註冊與登入流程：建立一般使用者帳號、驗證憑證並簽發 JWT。
 * 【技巧】組合 Spring Security 的 {@link AuthenticationManager}、{@link UserService} 與 {@link JwtTokenProvider}，將認證、帳號管理及 Token 技術分離。
 * 【概念】認證用例常橫跨多個元件；由 Application Service 編排流程可讓 Controller 保持薄，並使密碼驗證和 JWT 建構各自維持單一職責。
 * 【邊界】不處理 HTTP 狀態碼、不直接存取使用者資料，也不解析傳入 HTTP Header。
 */
@Service
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserService userService;

    public AuthService(AuthenticationManager authenticationManager,
                       JwtTokenProvider tokenProvider,
                       UserService userService) {
        this.authenticationManager = authenticationManager;
        this.tokenProvider = tokenProvider;
        this.userService = userService;
    }

    /**
     * 【職責】為新帳號套用預設 {@code USER} 角色，並委派使用者服務完成註冊。
     * 【技巧】使用不可變的 {@link Set#of(Object[])} 表達固定預設角色，避免呼叫端意外修改角色集合。
     * 【概念】公開註冊流程不接受客戶端直接指定角色，才能避免自行註冊為管理員；角色指派是用例規則而非 Controller 的 HTTP 細節。
     * 【邊界】不自行檢查帳號重複與雜湊密碼，這些責任由 {@link UserService} 處理。
     *
     * @param request 已通過格式驗證的帳號與密碼資料
     */
    public void register(RegisterRequest request) {
        userService.register(request.username(), request.password(), Set.of(Role.USER));
    }

    /**
     * 【職責】驗證帳密、擷取已授權角色，並產生 Bearer JWT 登入回應。
     * 【技巧】以 {@link UsernamePasswordAuthenticationToken} 呼叫 AuthenticationManager，再將 {@link GrantedAuthority} 映射為可放入 JWT claim 的字串列表。
     * 【概念】AuthenticationManager 集中處理密碼比對與失敗語意；取得成功的 Authentication 後才簽 Token，可避免以未驗證的請求資料建立憑證。
     * 【邊界】不直接比對密碼、不決定 HTTP 回應狀態，也不將登入狀態寫入 Session。
     *
     * @param request 含帳號與密碼的登入資料
     * @return 含 Bearer Token、帳號及已授權角色的回應
     */
    public JwtResponse login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.username(), request.password()));
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        String token = tokenProvider.generateToken(authentication.getName(), roles);
        return JwtResponse.bearer(token, authentication.getName(), roles);
    }
}
