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
 * 單元測試：覆蓋 {@link com.trading.security.service.UserService}。
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;
    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    UserService userService;

    // USER-UNIT-001
    @Test
    void register_whenUsernameAvailable_encodesPasswordAndSaves() {
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

    // USER-UNIT-002
    @Test
    void register_whenUsernameExists_throwsUsernameAlreadyExists() {
        when(userRepository.existsByUsername("alice")).thenReturn(true);

        assertThatThrownBy(() -> userService.register("alice", "secret123", Set.of(Role.USER)))
                .isInstanceOf(UsernameAlreadyExistsException.class);

        verify(userRepository, never()).save(any());
    }

    @Test
    void loadUserByUsername_whenFound_returnsUserDetailsWithRoles() {
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
