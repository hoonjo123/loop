package world.loop.domain.auth.handler;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.beans.factory.annotation.Value;
import world.loop.domain.auth.token.RefreshTokenService;
import world.loop.domain.auth.token.AuthenticationCookieService;
import world.loop.domain.user.entity.AuthProvider;
import world.loop.domain.user.entity.User;
import world.loop.domain.user.repository.UserRepository;

@Component
@RequiredArgsConstructor
public class GoogleOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final UserRepository userRepository;
    private final RefreshTokenService refreshTokenService;
    private final AuthenticationCookieService authenticationCookieService;

    @Value("${auth.frontend-url}")
    private String frontendUrl;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        OAuth2User principal = (OAuth2User) authentication.getPrincipal();
        String email = principal.getAttribute("email");
        if (email == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Google account email is required.");
            return;
        }

        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(User.builder()
                        .email(email)
                        .nickname(createNickname(principal.getAttributes()))
                        .authProvider(AuthProvider.GOOGLE)
                        .build()));
        RefreshTokenService.TokenPair tokens = refreshTokenService.issue(user.getId());
        authenticationCookieService.addTokenCookies(response, tokens);
        response.sendRedirect(frontendUrl);
    }

    private String createNickname(Map<String, Object> attributes) {
        String base = String.valueOf(attributes.getOrDefault("name", "loop"))
                .replaceAll("[^가-힣a-zA-Z0-9]", "");
        if (base.isBlank()) {
            base = "loop";
        }
        return (base.length() > 8 ? base.substring(0, 8) : base) + System.currentTimeMillis() % 10_000;
    }

}
