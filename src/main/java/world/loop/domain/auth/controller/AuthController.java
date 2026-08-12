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
import world.loop.domain.auth.dto.req.EmailRequest;
import world.loop.domain.auth.dto.req.LoginRequest;
import world.loop.domain.auth.dto.req.RefreshRequest;
import world.loop.domain.auth.dto.req.SignUpRequest;
import world.loop.domain.auth.dto.req.VerificationRequest;
import world.loop.domain.auth.dto.res.TokenResponse;
import world.loop.domain.auth.service.AuthService;
import world.loop.domain.mail.service.EmailVerificationService;
import world.loop.domain.auth.token.RefreshTokenService;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final RefreshTokenService refreshTokenService;

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
    public TokenResponse signUp(@Valid @RequestBody SignUpRequest request) {
        return TokenResponse.from(authService.signUp(request.email(), request.password(), request.nickname()));
    }

    @PostMapping("/login")
    public TokenResponse login(@Valid @RequestBody LoginRequest request) {
        return TokenResponse.from(authService.login(request.email(), request.password()));
    }

    @PostMapping("/refresh")
    public TokenResponse refresh(@Valid @RequestBody RefreshRequest request) {
        return TokenResponse.from(refreshTokenService.rotate(request.refreshToken()));
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
}
