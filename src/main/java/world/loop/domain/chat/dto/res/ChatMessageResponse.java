package world.loop.domain.chat.dto.res;

import java.time.LocalDateTime;
import world.loop.domain.chat.entity.ChatMessage;
import world.loop.domain.chat.entity.MessageType;

public record ChatMessageResponse(
        Long id,
        Long roomId,
        Long senderId,
        String senderNickname,
        String senderProfileImageUrl,
        MessageType messageType,
        String content,
        String imageUrl,
        LocalDateTime createdAt,
        boolean deleted
) {

    public static ChatMessageResponse from(ChatMessage message) {
        boolean deleted = message.getDeletedAt() != null;
        return new ChatMessageResponse(
                message.getId(),
                message.getRoom().getId(),
                message.getSender() == null ? null : message.getSender().getId(),
                message.getSender() == null ? null : message.getSender().getNickname(),
                message.getSender() == null ? null : message.getSender().getProfileImageUrl(),
                message.getMessageType(),
                deleted ? null : message.getContent(),
                deleted ? null : message.getImageUrl(),
                message.getCreatedAt(),
                deleted
        );
    }
}
