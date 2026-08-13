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
import world.loop.global.exception.BusinessException;
import world.loop.global.exception.ErrorCode;

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
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        emailVerificationService.requireVerified(email);
        User user = userRepository.save(User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .nickname(nickname)
                .nicknameConfigured(true)
                .authProvider(AuthProvider.LOCAL)
                .build());
        return refreshTokenService.issue(user.getId());
    }

    public RefreshTokenService.TokenPair login(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("Invalid credentials."));
        if (user.getPasswordHash() == null || !passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid credentials.");
        }
        return refreshTokenService.issue(user.getId());
    }

    @Transactional(readOnly = true)
    public boolean isNicknameAvailable(String nickname) {
        return !userRepository.existsByNickname(nickname);
    }

    @Transactional(readOnly = true)
    public UserSession session(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found."));
        return new UserSession(user.getId(), user.getNickname(), user.isNicknameConfigured());
    }

    public record UserSession(Long userId, String nickname, boolean nicknameConfigured) {
    }
}
