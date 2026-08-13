package world.loop.domain.chat.dto.res;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import world.loop.domain.chat.entity.ChatRoom;
import world.loop.domain.chat.entity.ChatRoomStatus;
import world.loop.domain.chat.entity.ChatRoomType;
import world.loop.domain.chat.entity.RoomDurationType;

public record ChatRoomResponse(
        Long id,
        ChatRoomType roomType,
        RoomDurationType durationType,
        String title,
        String description,
        String regionLabel,
        BigDecimal latitude,
        BigDecimal longitude,
        LocalDateTime expiresAt,
        ChatRoomStatus status,
        Long ownerId,
        String ownerNickname,
        long memberCount,
        boolean joined
) {

    public static ChatRoomResponse of(ChatRoom room, long memberCount, boolean joined) {
        return new ChatRoomResponse(
                room.getId(),
                room.getRoomType(),
                room.getDurationType(),
                room.getTitle(),
                room.getDescription(),
                room.getRegionLabel(),
                room.getLatitude(),
                room.getLongitude(),
                room.getExpiresAt(),
                room.getStatus(),
                room.getCreatedBy().getId(),
                room.getCreatedBy().getNickname(),
                memberCount,
                joined
        );
    }
}
