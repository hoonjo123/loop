package world.loop.domain.auth.token;

import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import world.loop.config.JwtProperties;
import world.loop.global.exception.RefreshTokenReuseException;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String SESSION_PREFIX = "auth:refresh:";
    private static final String USER_SESSIONS_PREFIX = "auth:refresh:user:";

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;

    public TokenPair issue(Long userId) {
        String sessionId = UUID.randomUUID().toString();
        String refreshToken = jwtTokenProvider.createRefreshToken(userId, sessionId);
        save(userId, sessionId, refreshToken);
        return new TokenPair(jwtTokenProvider.createAccessToken(userId), refreshToken);
    }

    public TokenPair rotate(String refreshToken) {
        Claims claims = jwtTokenProvider.parse(refreshToken);
        if (!"refresh".equals(claims.get("type", String.class))) {
            throw new IllegalArgumentException("Refresh token is required.");
        }
        Long userId = Long.valueOf(claims.getSubject());
        String sessionId = claims.get("sid", String.class);
        String key = sessionKey(userId, sessionId);
        String storedHash = redisTemplate.opsForValue().get(key);
        if (storedHash == null || !storedHash.equals(hash(refreshToken))) {
            revokeAll(userId);
            throw new RefreshTokenReuseException();
        }

        redisTemplate.delete(key);
        redisTemplate.opsForSet().remove(userSessionsKey(userId), sessionId);
        return issue(userId);
    }

    public void revoke(String refreshToken) {
        Claims claims = jwtTokenProvider.parse(refreshToken);
        if ("refresh".equals(claims.get("type", String.class))) {
            Long userId = Long.valueOf(claims.getSubject());
            String sessionId = claims.get("sid", String.class);
            redisTemplate.delete(sessionKey(userId, sessionId));
            redisTemplate.opsForSet().remove(userSessionsKey(userId), sessionId);
        }
    }

    private void save(Long userId, String sessionId, String refreshToken) {
        Duration ttl = jwtProperties.refreshTokenTtl();
        redisTemplate.opsForValue().set(sessionKey(userId, sessionId), hash(refreshToken), ttl);
        redisTemplate.opsForSet().add(userSessionsKey(userId), sessionId);
        redisTemplate.expire(userSessionsKey(userId), ttl);
    }

    private void revokeAll(Long userId) {
        Set<String> sessionIds = redisTemplate.opsForSet().members(userSessionsKey(userId));
        if (sessionIds != null) {
            sessionIds.forEach(sessionId -> redisTemplate.delete(sessionKey(userId, sessionId)));
        }
        redisTemplate.delete(userSessionsKey(userId));
    }

    private String sessionKey(Long userId, String sessionId) {
        return SESSION_PREFIX + userId + ':' + sessionId;
    }

    private String userSessionsKey(Long userId) {
        return USER_SESSIONS_PREFIX + userId;
    }

    private String hash(String value) {
        try {
            return java.util.HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public record TokenPair(String accessToken, String refreshToken) {
    }
}
