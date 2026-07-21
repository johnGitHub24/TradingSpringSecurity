package com.trading.security.service;

import com.trading.security.domain.Role;
import com.trading.security.entity.UserEntity;
import com.trading.security.exception.UsernameAlreadyExistsException;
import com.trading.security.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * 【職責】管理使用者帳號建立，並實作 Spring Security 所需的 {@link UserDetailsService} 使用者載入契約。
 * 【技巧】透過 {@link PasswordEncoder} 雜湊密碼、以 Repository 檢查唯一帳號，再將領域角色轉換為 Spring Security 的 GrantedAuthority。
 * 【概念】密碼不可儲存為明文；將帳號存取與 Security 介面轉換集中於此服務，可讓認證框架取得標準 UserDetails，同時不使其侵入 Controller。
 * 【邊界】不簽發 JWT、不組裝 HTTP 回應，也不定義登入端點。
 */
@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * 【職責】確認帳號唯一後，以雜湊密碼與指定角色建立使用者。
     * 【技巧】使用 {@link PasswordEncoder#encode(CharSequence)} 產生不可逆雜湊，並在交易中透過 Repository 寫入新 Entity。
     * 【概念】帳號唯一性必須由服務規則保護；雜湊而非加密的密碼可避免資料庫外洩時還原明文，驗證則交由編碼器的 matches 流程。
     * 【邊界】不信任或解析 HTTP 請求；呼叫端須先決定可授予的角色。
     *
     * @param username 要建立且必須唯一的帳號
     * @param rawPassword 僅在寫入前使用的明文密碼
     * @param roles 要賦予新帳號的角色集合
     * @return 已持久化的使用者 Entity
     * @throws UsernameAlreadyExistsException 當帳號已存在時
     */
    @Transactional
    public UserEntity register(String username, String rawPassword, Set<Role> roles) {
        if (userRepository.existsByUsername(username)) {
            throw new UsernameAlreadyExistsException(username);
        }
        UserEntity user = UserEntity.builder()
                .username(username)
                .password(passwordEncoder.encode(rawPassword))
                .roles(roles)
                .build();
        return userRepository.save(user);
    }

    /**
     * 【職責】依帳號載入可供 Spring Security 認證使用的 {@link UserDetails}。
     * 【技巧】實作框架的回呼介面，將領域 {@link Role} 映射為帶有 {@code ROLE_} 前綴的 {@link SimpleGrantedAuthority}。
     * 【概念】角色與授權機關的格式不同；在適配層轉換可保留領域 enum 的簡潔性，並符合 {@code hasRole} 對 {@code ROLE_} 前綴的慣例。
     * 【邊界】不驗證傳入密碼；AuthenticationManager 會以本方法回傳的雜湊密碼完成比對。
     *
     * @param username 要載入的帳號
     * @return 含雜湊密碼與授權清單的 Spring Security 使用者
     * @throws UsernameNotFoundException 當帳號不存在時
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        UserEntity user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("找不到使用者: " + username));
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role.name()))
                .toList();
        return new User(user.getUsername(), user.getPassword(), authorities);
    }
}
