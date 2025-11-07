package com.comp5348.store.service.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import com.comp5348.store.model.auth.User;
import com.comp5348.store.model.auth.UserRole;
import com.comp5348.store.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@ExtendWith(MockitoExtension.class)
class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = User.builder()
            .id(1L)
            .name("Test User")
            .email("test@example.com")
            .password("hashedPassword")
            .role(UserRole.CUSTOMER)
            .enabled(true)
            .build();
    }

    @Test
    void loadUserByUsername_shouldReturnUserDetails_whenUserExists() {
        // Given
        when(userRepository.findByName("Test User")).thenReturn(
            Optional.of(testUser)
        );

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(
            "Test User"
        );

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.getUsername()).isEqualTo("Test User");
        assertThat(userDetails.getPassword()).isEqualTo("hashedPassword");
        assertThat(userDetails.getAuthorities()).hasSize(1);
        assertThat(userDetails.isEnabled()).isTrue();
    }

    @Test
    void loadUserByUsername_shouldThrowException_whenUserNotFound() {
        // Given
        when(userRepository.findByName(anyString())).thenReturn(
            Optional.empty()
        );

        // When/Then
        assertThatThrownBy(() ->
            customUserDetailsService.loadUserByUsername("No name")
        )
            .isInstanceOf(UsernameNotFoundException.class)
            .hasMessageContaining("User not found with username: No name");
    }

    @Test
    void loadUserByUsername_shouldReturnDisabledUser_whenUserIsDisabled() {
        // Given
        User disabledUser = User.builder()
            .id(2L)
            .name("Disabled User")
            .email("disabled@example.com")
            .password("hashedPassword")
            .role(UserRole.CUSTOMER)
            .enabled(false)
            .build();

        when(userRepository.findByName("Disabled User")).thenReturn(
            Optional.of(disabledUser)
        );

        // When
        UserDetails userDetails = customUserDetailsService.loadUserByUsername(
            "Disabled User"
        );

        // Then
        assertThat(userDetails).isNotNull();
        assertThat(userDetails.isEnabled()).isFalse();
    }
}
