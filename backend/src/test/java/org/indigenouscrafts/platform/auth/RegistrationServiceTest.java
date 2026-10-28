package org.indigenouscrafts.platform.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.indigenouscrafts.platform.auth.dto.RegisterRequest;
import org.indigenouscrafts.platform.user.AppUser;
import org.indigenouscrafts.platform.user.UserRepository;
import org.indigenouscrafts.platform.user.UserStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class RegistrationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private RegistrationService registrationService;

    private final RegisterRequest request =
            new RegisterRequest("seda", "seda@example.com", "sifre12345");

    @Test
    void registersNewUserWithHashedPassword() {
        when(userRepository.existsByEmailIgnoreCase("seda@example.com")).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase("seda")).thenReturn(false);
        when(passwordEncoder.encode("sifre12345")).thenReturn("hashed-password");
        when(userRepository.save(any(AppUser.class))).thenAnswer(call -> call.getArgument(0));

        AppUser user = registrationService.register(request);

        assertThat(user.getPasswordHash()).isEqualTo("hashed-password");
        assertThat(user.getStatus()).isEqualTo(UserStatus.PENDING);
    }

    @Test
    void rejectsTakenEmail() {
        when(userRepository.existsByEmailIgnoreCase("seda@example.com")).thenReturn(true);

        assertThatThrownBy(() -> registrationService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .extracting("field").isEqualTo("email");
        verify(userRepository, never()).save(any());
    }

    @Test
    void rejectsTakenUsername() {
        when(userRepository.existsByEmailIgnoreCase("seda@example.com")).thenReturn(false);
        when(userRepository.existsByUsernameIgnoreCase("seda")).thenReturn(true);

        assertThatThrownBy(() -> registrationService.register(request))
                .isInstanceOf(UserAlreadyExistsException.class)
                .extracting("field").isEqualTo("username");
        verify(userRepository, never()).save(any());
    }
}