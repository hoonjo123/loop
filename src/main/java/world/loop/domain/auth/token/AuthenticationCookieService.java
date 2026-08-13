package world.loop.domain.auth.token;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;
import world.loop.config.JwtProperties;

@Service
@RequiredArgsConstructor
public class AuthenticationCookieService {

    public static final String ACCESS_TOKEN_COOKIE = "loop_access_token";
    public static final String REFRESH_TOKEN_COOKIE = "loop_refresh_token";

    private final JwtProperties jwtProperties;

    public void addTokenCookies(HttpServletResponse response, RefreshTokenService.TokenPair tokens) {
        addCookie(response, ACCESS_TOKEN_COOKIE, tokens.accessToken(), jwtProperties.accessTokenTtl().toSeconds());
        addCookie(response, REFRESH_TOKEN_COOKIE, tokens.refreshToken(), jwtProperties.refreshTokenTtl().toSeconds());
    }

    public void clearTokenCookies(HttpServletResponse response) {
        addCookie(response, ACCESS_TOKEN_COOKIE, "", 0);
        addCookie(response, REFRESH_TOKEN_COOKIE, "", 0);
    }

    private void addCookie(HttpServletResponse response, String name, String value, long maxAge) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(true)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
