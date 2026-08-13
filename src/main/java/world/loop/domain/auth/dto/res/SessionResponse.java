package world.loop.domain.auth.dto.res;

import world.loop.domain.auth.service.AuthService;

public record SessionResponse(
        Long userId,
        String nickname,
        boolean nicknameConfigured
) {
    public static SessionResponse from(AuthService.UserSession session) {
        return new SessionResponse(session.userId(), session.nickname(), session.nicknameConfigured());
    }
}
