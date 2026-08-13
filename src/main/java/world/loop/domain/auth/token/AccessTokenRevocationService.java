package world.loop.domain.auth.token;

import io.jsonwebtoken.Claims;
import java.time.Duration;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccessTokenRevocationService {

    private static final String REVOKED_ACCESS_TOKEN_PREFIX = "auth:access:revoked:";

    private final StringRedisTemplate redisTemplate;
    private final JwtTokenProvider jwtTokenProvider;

    public void revoke(String accessToken) {
        Claims claims = jwtTokenProvider.parse(accessToken);
        if (!"access".equals(claims.get("type", String.class))) {
            return;
        }

        Duration remainingTtl = Duration.between(Instant.now(), claims.getExpiration().toInstant());
        if (remainingTtl.isPositive()) {
            redisTemplate.opsForValue().set(revocationKey(claims.getId()), "1", remainingTtl);
        }
    }

    public boolean isRevoked(String tokenId) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(revocationKey(tokenId)));
    }

    private String revocationKey(String tokenId) {
        return REVOKED_ACCESS_TOKEN_PREFIX + tokenId;
    }
}
