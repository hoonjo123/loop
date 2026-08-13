package world.loop.domain.user.dto.res;

import java.time.LocalDateTime;
import world.loop.domain.user.entity.AuthProvider;
import world.loop.domain.user.entity.User;

public record ProfileResponse(
        Long id,
        String email,
        String nickname,
        String profileImageUrl,
        String introduction,
        String activityArea,
        AuthProvider authProvider,
        LocalDateTime createdAt
) {

    public static ProfileResponse from(User user) {
        return new ProfileResponse(
                user.getId(),
                user.getEmail(),
                user.getNickname(),
                user.getProfileImageUrl(),
                user.getIntroduction(),
                user.getActivityArea(),
                user.getAuthProvider(),
                user.getCreatedAt()
        );
    }
}
