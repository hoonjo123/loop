package world.loop.domain.auth.token;

import io.jsonwebtoken.Claims;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.UUID;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import world.loop.config.JwtProperties;
import world.loop.config.RedisScriptConfig.RefreshTokenRedisScripts;
import world.loop.global.exception.RefreshTokenReuseException;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private static final String SESSION_PREFIX = "auth:refresh:";
    private static final String USER_SESSIONS_PREFIX = "auth:refresh:user:";

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final RefreshTokenRedisScripts redisScripts;

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
        String currentSessionId = claims.get("sid", String.class);
        String nextSessionId = UUID.randomUUID().toString();
        String nextRefreshToken = jwtTokenProvider.createRefreshToken(userId, nextSessionId);
        Long rotated = redisTemplate.execute(
                redisScripts.rotate(),
                List.of(
                        sessionKey(userId, currentSessionId),
                        sessionKey(userId, nextSessionId),
                        userSessionsKey(userId)
                ),
                hash(refreshToken),
                currentSessionId,
                nextSessionId,
                hash(nextRefreshToken),
                String.valueOf(jwtProperties.refreshTokenTtl().toMillis())
        );
        if (!Long.valueOf(1).equals(rotated)) {
            revokeAll(userId);
            throw new RefreshTokenReuseException();
        }
        return new TokenPair(jwtTokenProvider.createAccessToken(userId), nextRefreshToken);
    }

    public void revoke(String refreshToken) {
        Claims claims = jwtTokenProvider.parse(refreshToken);
        if ("refresh".equals(claims.get("type", String.class))) {
            Long userId = Long.valueOf(claims.getSubject());
            String sessionId = claims.get("sid", String.class);
            redisTemplate.execute(
                    redisScripts.revoke(),
                    List.of(sessionKey(userId, sessionId), userSessionsKey(userId)),
                    sessionId
            );
        }
    }

    private void save(Long userId, String sessionId, String refreshToken) {
        redisTemplate.execute(
                redisScripts.save(),
                List.of(sessionKey(userId, sessionId), userSessionsKey(userId)),
                hash(refreshToken),
                sessionId,
                String.valueOf(jwtProperties.refreshTokenTtl().toMillis())
        );
    }

    private void revokeAll(Long userId) {
        redisTemplate.execute(
                redisScripts.revokeAll(),
                List.of(userSessionsKey(userId)),
                SESSION_PREFIX + userId + ':'
        );
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
