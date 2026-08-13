package world.loop.domain.auth.controller;

import jakarta.validation.Valid;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import world.loop.domain.auth.dto.req.EmailRequest;
import world.loop.domain.auth.dto.req.LoginRequest;
import world.loop.domain.auth.dto.req.NicknameCheckRequest;
import world.loop.domain.auth.dto.req.RefreshRequest;
import world.loop.domain.auth.dto.req.SignUpRequest;
import world.loop.domain.auth.dto.req.VerificationRequest;
import world.loop.domain.auth.dto.res.TokenResponse;
import world.loop.domain.auth.dto.res.EmailVerificationResponse;
import world.loop.domain.auth.dto.res.NicknameAvailabilityResponse;
import world.loop.domain.auth.dto.res.SessionResponse;
import world.loop.domain.auth.service.AuthService;
import world.loop.domain.mail.service.EmailVerificationService;
import world.loop.domain.auth.token.RefreshTokenService;
import world.loop.domain.auth.token.AuthenticationCookieService;
import world.loop.domain.auth.token.AccessTokenRevocationService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationCookieService authenticationCookieService;
    private final AccessTokenRevocationService accessTokenRevocationService;

    @PostMapping("/email-verifications")
    public EmailVerificationResponse sendVerificationCode(@Valid @RequestBody EmailRequest request) {
        emailVerificationService.sendCode(request.email());
        return new EmailVerificationResponse(
                emailVerificationService.codeTtlSeconds(),
                emailVerificationService.requestCooldownSeconds()
        );
    }

    @PostMapping("/email-verifications/confirm")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void verifyEmail(@Valid @RequestBody VerificationRequest request) {
        emailVerificationService.verify(request.email(), request.code());
    }

    @PostMapping("/nicknames/check")
    public NicknameAvailabilityResponse checkNickname(@Valid @RequestBody NicknameCheckRequest request) {
        return new NicknameAvailabilityResponse(authService.isNicknameAvailable(request.nickname()));
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
    public void logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = findCookieValue(request, AuthenticationCookieService.REFRESH_TOKEN_COOKIE);
        String accessToken = findCookieValue(request, AuthenticationCookieService.ACCESS_TOKEN_COOKIE);

        revokeRefreshToken(refreshToken);
        revokeAccessToken(accessToken);
        authenticationCookieService.clearTokenCookies(response);
    }

    @GetMapping("/session")
    public SessionResponse session(@AuthenticationPrincipal Long userId) {
        return SessionResponse.from(authService.session(userId));
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

    private void revokeRefreshToken(String refreshToken) {
        if (refreshToken == null) {
            return;
        }
        try {
            refreshTokenService.revoke(refreshToken);
        } catch (JwtException ignored) {
            // 이미 만료되었거나 유효하지 않은 토큰은 재사용할 수 없으므로 로그아웃을 계속한다.
        }
    }

    private void revokeAccessToken(String accessToken) {
        if (accessToken == null) {
            return;
        }
        try {
            accessTokenRevocationService.revoke(accessToken);
        } catch (JwtException ignored) {
            // 이미 만료되었거나 유효하지 않은 토큰은 블랙리스트에 저장할 필요가 없다.
        }
    }
}
