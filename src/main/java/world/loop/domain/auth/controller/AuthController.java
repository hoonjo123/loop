package world.loop.domain.auth.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import world.loop.domain.auth.dto.req.EmailRequest;
import world.loop.domain.auth.dto.req.LoginRequest;
import world.loop.domain.auth.dto.req.RefreshRequest;
import world.loop.domain.auth.dto.req.SignUpRequest;
import world.loop.domain.auth.dto.req.VerificationRequest;
import world.loop.domain.auth.dto.res.TokenResponse;
import world.loop.domain.auth.service.AuthService;
import world.loop.domain.mail.service.EmailVerificationService;
import world.loop.domain.auth.token.RefreshTokenService;
import world.loop.domain.auth.token.AuthenticationCookieService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationCookieService authenticationCookieService;

    @PostMapping("/email-verifications")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void sendVerificationCode(@Valid @RequestBody EmailRequest request) {
        emailVerificationService.sendCode(request.email());
    }

    @PostMapping("/email-verifications/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@Valid @RequestBody VerificationRequest request) {
        emailVerificationService.verify(request.email(), request.code());
    }

    @PostMapping("/sign-up")
    @ResponseStatus(HttpStatus.CREATED)
    public TokenResponse signUp(@Valid @RequestBody SignUpRequest request, HttpServletResponse response) {
        RefreshTokenService.TokenPair tokens = authService.signUp(request.email(), request.password(), request.nickname());
        authenticationCookieService.addTokenCookies(response, tokens);
        return TokenResponse.from(tokens);
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request, HttpServletResponse response) {
        RefreshTokenService.TokenPair tokens = authService.login(request.email(), request.password());
        authenticationCookieService.addTokenCookies(response, tokens);
        return TokenResponse.from(tokens);
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request, HttpServletResponse response) {
        RefreshTokenService.TokenPair tokens = refreshTokenService.rotate(request.refreshToken());
        authenticationCookieService.addTokenCookies(response, tokens);
        return TokenResponse.from(tokens);
    }

    @PostMapping("/refresh/cookie")
    public TokenResponse refreshFromCookie(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = findCookieValue(request, AuthenticationCookieService.REFRESH_TOKEN_COOKIE);
        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh token is required.");
        }
        RefreshTokenService.TokenPair tokens = refreshTokenService.rotate(refreshToken);
        authenticationCookieService.addTokenCookies(response, tokens);
        return TokenResponse.from(tokens);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void logout(@Valid @RequestBody RefreshRequest request) {
        refreshTokenService.revoke(request.refreshToken());
    }

    @GetMapping("/session")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void session() {
    }

    private String findCookieValue(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
