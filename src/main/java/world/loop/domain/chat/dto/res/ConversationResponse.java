package world.loop.domain.chat.dto.res;

import java.time.LocalDateTime;
import world.loop.domain.chat.entity.ChatRoomType;

public record ConversationResponse(
        Long roomId,
        ChatRoomType roomType,
        String title,
        String regionLabel,
        long memberCount,
        String lastMessage,
        LocalDateTime lastMessageAt,
        long unreadCount,
        Long targetUserId,
        String targetProfileImageUrl
) {
}
