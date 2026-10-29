package org.indigenouscrafts.platform.auth;

import org.indigenouscrafts.platform.auth.dto.LoginRequest;
import org.indigenouscrafts.platform.auth.dto.TokenResponse;
import org.indigenouscrafts.platform.security.TokenService;
import org.indigenouscrafts.platform.user.AppUser;
import org.indigenouscrafts.platform.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LoginService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;

    public LoginService(UserRepository userRepository, PasswordEncoder passwordEncoder,
                        TokenService tokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(InvalidCredentialsException::new);

        if (user.getPasswordHash() == null
                || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new InvalidCredentialsException();
        }

        String token = tokenService.createToken(user);
        return TokenResponse.bearer(token, tokenService.ttlSeconds());
    }
}