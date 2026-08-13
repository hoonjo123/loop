package world.loop.domain.user.dto.res;

import java.time.LocalDateTime;
import world.loop.domain.user.entity.UserBlock;

public record BlockedUserResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        LocalDateTime blockedAt
) {

    public static BlockedUserResponse from(UserBlock block) {
        return new BlockedUserResponse(
                block.getBlocked().getId(),
                block.getBlocked().getNickname(),
                block.getBlocked().getProfileImageUrl(),
                block.getCreatedAt()
        );
    }
}
