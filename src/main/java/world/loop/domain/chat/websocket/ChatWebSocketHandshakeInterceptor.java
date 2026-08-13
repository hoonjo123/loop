package world.loop.domain.chat.websocket;

import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;
import world.loop.domain.auth.token.AccessTokenRevocationService;
import world.loop.domain.auth.token.AuthenticationCookieService;
import world.loop.domain.auth.token.JwtTokenProvider;

@Component
@RequiredArgsConstructor
public class ChatWebSocketHandshakeInterceptor implements HandshakeInterceptor {

    public static final String USER_ID_ATTRIBUTE = "userId";
    public static final String TOKEN_ID_ATTRIBUTE = "accessTokenId";
    public static final String TOKEN_EXPIRATION_ATTRIBUTE = "accessTokenExpiration";

    private final JwtTokenProvider jwtTokenProvider;
    private final AccessTokenRevocationService accessTokenRevocationService;

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes
    ) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }
        String token = findAccessToken(servletRequest.getServletRequest());
        if (token == null) {
            return false;
        }
        try {
            Claims claims = jwtTokenProvider.parse(token);
            if (!"access".equals(claims.get("type", String.class))
                    || accessTokenRevocationService.isRevoked(claims.getId())) {
                return false;
            }
            attributes.put(USER_ID_ATTRIBUTE, Long.valueOf(claims.getSubject()));
            attributes.put(TOKEN_ID_ATTRIBUTE, claims.getId());
            attributes.put(TOKEN_EXPIRATION_ATTRIBUTE, claims.getExpiration().toInstant());
            return true;
        } catch (RuntimeException exception) {
            return false;
        }
    }

    @Override
    public void afterHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Exception exception
    ) {
    }

    private String findAccessToken(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (AuthenticationCookieService.ACCESS_TOKEN_COOKIE.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
