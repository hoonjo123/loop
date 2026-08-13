package world.loop.domain.chat.dto.res;

import world.loop.domain.chat.entity.ChatMemberRole;
import world.loop.domain.chat.entity.ChatRoomMember;

public record ChatMemberResponse(
        Long userId,
        String nickname,
        String profileImageUrl,
        String activityArea,
        String introduction,
        ChatMemberRole role
) {

    public static ChatMemberResponse from(ChatRoomMember member) {
        return new ChatMemberResponse(
                member.getUser().getId(),
                member.getUser().getNickname(),
                member.getUser().getProfileImageUrl(),
                member.getUser().getActivityArea(),
                member.getUser().getIntroduction(),
                member.getRole()
        );
    }
}
