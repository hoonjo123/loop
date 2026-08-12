package world.loop.domain.auth.dto.res;

import world.loop.domain.auth.token.RefreshTokenService;

public record TokenResponse(
        String accessToken,
        String refreshToken
) {
    public static TokenResponse from(RefreshTokenService.TokenPair pair) {
        return new TokenResponse(pair.accessToken(), pair.refreshToken());
    }
}
