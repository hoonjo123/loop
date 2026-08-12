package world.loop.domain.auth.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import world.loop.domain.mail.service.EmailVerificationService;
import world.loop.domain.auth.token.RefreshTokenService;
import world.loop.domain.user.entity.AuthProvider;
import world.loop.domain.user.entity.User;
import world.loop.domain.user.repository.UserRepository;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailVerificationService emailVerificationService;
    private final RefreshTokenService refreshTokenService;

    @Transactional
    public RefreshTokenService.TokenPair signUp(String email, String password, String nickname) {
        emailVerificationService.requireVerified(email);
        if (userRepository.existsByEmail(email) || userRepository.existsByNickname(nickname)) {
            throw new IllegalArgumentException("Email or nickname already exists.");
        }
        User user = userRepository.save(User.builder().email(email).passwordHash(passwordEncoder.encode(password)).nickname(nickname).authProvider(AuthProvider.LOCAL).build());
        return refreshTokenService.issue(user.getId());
    }

    public RefreshTokenService.TokenPair login(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Invalid credentials."));
        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }
        return refreshTokenService.issue(user.getId());
    }
}
