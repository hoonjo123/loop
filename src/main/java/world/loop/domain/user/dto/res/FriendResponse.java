package world.loop.domain.user.dto.res;

import java.time.LocalDateTime;
import world.loop.domain.user.entity.User;
import world.loop.domain.user.entity.UserFriend;

public record FriendResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        String activityArea,
        LocalDateTime friendedAt
) {

    public static FriendResponse from(UserFriend friend, Long currentUserId) {
        User target = friend.getUserOne().getId().equals(currentUserId)
                ? friend.getUserTwo()
                : friend.getUserOne();
        return new FriendResponse(
                target.getId(),
                target.getNickname(),
                target.getProfileImageUrl(),
                target.getActivityArea(),
                friend.getCreatedAt()
        );
    }
}
