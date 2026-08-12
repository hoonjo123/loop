package world.loop.domain.auth.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import world.loop.config.JwtProperties;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final JwtProperties properties;
    private SecretKey key;

    @PostConstruct
    void initializeKey() {
        this.key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(properties.secret()));
    }

    public String createAccessToken(Long userId) {
        return createToken(userId, null, "access", properties.accessTokenTtl());
    }

    public String createRefreshToken(Long userId, String sessionId) {
        return createToken(userId, sessionId, "refresh", properties.refreshTokenTtl());
    }

    public Claims parse(String token) {
        return Jwts.parser().verifyWith(key).build().parseSignedClaims(token).getPayload();
    }

    private String createToken(Long userId, String sessionId, String type, java.time.Duration ttl) {
        Instant now = Instant.now();
        var builder = Jwts.builder()
                .subject(String.valueOf(userId))
                .id(UUID.randomUUID().toString())
                .claim("type", type)
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(ttl)));
        if (sessionId != null) {
            builder.claim("sid", sessionId);
        }
        return builder.signWith(key).compact();
    }
}
