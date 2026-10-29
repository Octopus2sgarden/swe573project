package org.indigenouscrafts.platform.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.indigenouscrafts.platform.auth.dto.LoginRequest;
import org.indigenouscrafts.platform.auth.dto.TokenResponse;
import org.indigenouscrafts.platform.security.TokenService;
import org.indigenouscrafts.platform.user.AppUser;
import org.indigenouscrafts.platform.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class LoginServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private LoginService loginService;

    @Test
    void login_withCorrectCredentials_returnsToken() {
        AppUser user = AppUser.local("seda", "seda@example.com", "hashed-password");

        when(userRepository.findByEmailIgnoreCase("seda@example.com"))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("sifre12345", "hashed-password"))
            .thenReturn(true);
        when(tokenService.createToken(user)).thenReturn("fake-token");
        when(tokenService.ttlSeconds()).thenReturn(3600L);

        TokenResponse response = loginService.login(
            new LoginRequest("seda@example.com", "sifre12345"));

        assertEquals("fake-token", response.accessToken());
        assertEquals("Bearer", response.tokenType());
        assertEquals(3600L, response.expiresIn());
    }
    @Test
    void login_withUnknownEmail_throwsInvalidCredentials() {
        when(userRepository.findByEmailIgnoreCase("nobody@example.com"))
            .thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class,
            () -> loginService.login(new LoginRequest("nobody@example.com", "sifre12345")));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(tokenService, never()).createToken(any());
    }
    @Test
    void login_withWrongPassword_throwsInvalidCredentials() {
        AppUser user = AppUser.local("seda", "seda@example.com", "hashed-password");

        when(userRepository.findByEmailIgnoreCase("seda@example.com"))
            .thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongsifre12345", "hashed-password"))
            .thenReturn(false);

        assertThrows(InvalidCredentialsException.class,
            () -> loginService.login(new LoginRequest("seda@example.com", "wrongsifre12345")));

        verify(tokenService, never()).createToken(any());
    }
@Test
    void login_withNullPasswordHash_throwsInvalidCredentials() {
        AppUser user = AppUser.local("seda", "seda@example.com", null);

        when(userRepository.findByEmailIgnoreCase("seda@example.com"))
            .thenReturn(Optional.of(user));

        assertThrows(InvalidCredentialsException.class,
            () -> loginService.login(new LoginRequest("seda@example.com", "seda12345")));

        verify(passwordEncoder, never()).matches(anyString(), anyString());
        verify(tokenService, never()).createToken(any());
    }

}