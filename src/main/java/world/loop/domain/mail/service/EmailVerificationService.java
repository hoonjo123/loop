package world.loop.domain.mail.service;

import java.security.SecureRandom;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import world.loop.domain.mail.config.MailVerificationProperties;
@Service
@RequiredArgsConstructor
public class EmailVerificationService {
    private static final String CODE_PREFIX = "auth:email:code:";
    private static final String VERIFIED_PREFIX = "auth:email:verified:";
    private static final String COOLDOWN_PREFIX = "auth:email:cooldown:";

    private final StringRedisTemplate redisTemplate;
    private final JavaMailSender mailSender;
    private final MailVerificationProperties properties;
    private final SecureRandom random = new SecureRandom();

    public void sendCode(String email) {
        if (Boolean.TRUE.equals(redisTemplate.hasKey(COOLDOWN_PREFIX + email))) {
            throw new IllegalStateException("Please wait before requesting another verification code.");
        }
        String code = "%06d".formatted(random.nextInt(1_000_000));
        redisTemplate.opsForValue().set(CODE_PREFIX + email, code, properties.codeTtl());
        redisTemplate.opsForValue().set(COOLDOWN_PREFIX + email, "1", properties.requestCooldown());
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("loop 이메일 인증번호");
        message.setText("인증번호는 %s 입니다. %d분 내에 입력해주세요.".formatted(code, properties.codeTtl().toMinutes()));
        mailSender.send(message);
    }

    public void verify(String email, String code) {
        String stored = redisTemplate.opsForValue().get(CODE_PREFIX + email);
        if (stored == null || !stored.equals(code)) {
            throw new IllegalArgumentException("Invalid or expired verification code.");
        }
        redisTemplate.delete(CODE_PREFIX + email);
        redisTemplate.opsForValue().set(VERIFIED_PREFIX + email, "1", properties.codeTtl());
    }

    public void requireVerified(String email) {
        if (!Boolean.TRUE.equals(redisTemplate.hasKey(VERIFIED_PREFIX + email))) {
            throw new IllegalStateException("Email verification is required.");
        }
        redisTemplate.delete(VERIFIED_PREFIX + email);
    }
}
