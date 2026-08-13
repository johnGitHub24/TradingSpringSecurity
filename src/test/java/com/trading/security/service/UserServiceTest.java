package com.trading.security.service;

import com.trading.security.domain.Role;
import com.trading.security.entity.UserEntity;
import com.trading.security.exception.UsernameAlreadyExistsException;
import com.trading.security.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 【職責】單元測試 {@link UserService}：註冊雜湊密碼、帳號衝突、載入 UserDetails。
 * 【技巧】Mock Repository 與 PasswordEncoder，不啟動 JPA／Security Filter。
 * 【概念】與 USER-001, AUTH-003 整合層同一契約：成功寫入雜湊；重複帳號拋衝突（HTTP 409）。
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    /**
     * CASE USER-001：帳號可用時編碼密碼並儲存 USER 角色。
     * Given: existsByUsername=false、encoder 回 encoded；When: register；Then: 持久化雜湊而非明文。
     */
    @Test
    void USER_001_register_whenUsernameAvailable_encodesPasswordAndSaves() {
        when(userRepository.existsByUsername("alice")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded");
        when(userRepository.save(any(UserEntity.class))).thenAnswer(inv -> {
            UserEntity u = inv.getArgument(0);
            u.setId(1L);
            return u;
        });

        UserEntity saved = userService.register("alice", "secret123", Set.of(Role.USER));

        assertThat(saved.getId()).isEqualTo(1L);
        ArgumentCaptor<UserEntity> captor = ArgumentCaptor.forClass(UserEntity.class);
        verify(userRepository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("encoded");
        assertThat(captor.getValue().getRoles()).containsExactly(Role.USER);
    }

    /**
     * CASE AUTH-003：重複 username 拋 UsernameAlreadyExistsException 且不 save。
     * Given: existsByUsername=true；When: register；Then: 衝突例外（整合層 409 USERNAME_EXISTS）。
     */
    @Test
    void AUTH_003_register_whenUsernameExists_throwsUsernameAlreadyExists() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("alice", "secret123", Set.of(Role.USER)))
                .isInstanceOf(UsernameAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    /**
     * CASE USER-001 / JWT-003：載入使用者時把領域角色轉成 ROLE_ 前綴。
     * Given: 既有 USER+ADMIN；When: loadUserByUsername；Then: authorities 含 ROLE_USER 與 ROLE_ADMIN。
     */
    @Test
    void USER_001_loadUserByUsername_whenFound_returnsUserDetailsWithRoles() {
        UserEntity user = UserEntity.builder()
                .username("alice")
                .password("encoded")
                .roles(Set.of(Role.USER, Role.ADMIN))
                .build();
        when(userRepository.findByUsername("alice")).thenReturn(Optional.of(user));

        UserDetails details = userService.loadUserByUsername("alice");

        assertThat(details.getUsername()).isEqualTo("alice");
        assertThat(details.getAuthorities()).extracting("authority")
                .containsExactlyInAnyOrder("ROLE_USER", "ROLE_ADMIN");
    }
}
