package org.indigenouscrafts.platform.auth;

import jakarta.validation.Valid;
import org.indigenouscrafts.platform.auth.dto.LoginRequest;
import org.indigenouscrafts.platform.auth.dto.RegisterRequest;
import org.indigenouscrafts.platform.auth.dto.TokenResponse;
import org.indigenouscrafts.platform.auth.dto.UserResponse;
import org.indigenouscrafts.platform.user.AppUser;
import org.indigenouscrafts.platform.user.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpHeaders;


@RestController
@RequestMapping("/api/auth")
public class AuthController {

    // Cookie name is shared with SecurityConfig, which reads the token from it.
    public static final String AUTH_COOKIE = "access_token";

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
    public ResponseEntity<Void> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse token = loginService.login(request);
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, authCookie(token.accessToken(), token.expiresIn()).toString())
                .build();
    }

    // Logout = tell the browser to delete the cookie (same name + same path, Max-Age 0).
    // JWT is stateless, so there is nothing to delete on the server side.
    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE, authCookie("", 0).toString())
                .build();
    }

    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        Long userId = Long.valueOf(jwt.getSubject());
        AppUser user = userService.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
        return UserResponse.from(user);
    }

    // One place builds the cookie, so login and logout always use the same name and path.
    // If the path differed, the browser would treat them as two different cookies
    // and logout would not remove the login cookie.
    private static ResponseCookie authCookie(String value, long maxAgeSeconds) {
        return ResponseCookie.from(AUTH_COOKIE, value)
                .httpOnly(true)       // JavaScript cannot read it (protects against XSS token theft)
                .secure(false)        // TODO: true after HTTPS deployment (M6)
                .sameSite("Strict")   // not sent on requests coming from other sites
                .path("/api")         // only sent to the API, not to static pages
                .maxAge(maxAgeSeconds)
                .build();
    }
}