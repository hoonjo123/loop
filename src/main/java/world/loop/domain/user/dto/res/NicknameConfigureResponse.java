package world.loop.domain.user.dto.res;

public record NicknameConfigureResponse(
        String nickname,
        boolean nicknameConfigured
) {
}
