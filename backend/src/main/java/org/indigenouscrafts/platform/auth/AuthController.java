package org.indigenouscrafts.platform.auth;

import jakarta.validation.Valid;
import org.indigenouscrafts.platform.auth.dto.LoginRequest;
import org.indigenouscrafts.platform.auth.dto.RegisterRequest;
import org.indigenouscrafts.platform.auth.dto.TokenResponse;
import org.indigenouscrafts.platform.auth.dto.UserResponse;
import org.indigenouscrafts.platform.user.AppUser;
import org.indigenouscrafts.platform.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final RegistrationService registrationService;
    private final LoginService loginService;
    private final UserService userService;

    public AuthController(RegistrationService registrationService, LoginService loginService,
                          UserService userService) {
        this.registrationService = registrationService;
        this.loginService = loginService;
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponse register(@Valid @RequestBody RegisterRequest request) {
        AppUser user = registrationService.register(request);
        return UserResponse.from(user);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return loginService.login(request);
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        AppUser user = userService.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return UserResponse.from(user);
    }
}