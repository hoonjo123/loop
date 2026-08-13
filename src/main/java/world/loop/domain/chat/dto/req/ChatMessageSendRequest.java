package world.loop.domain.chat.dto.req;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import world.loop.domain.chat.entity.MessageType;

public record ChatMessageSendRequest(
        @NotNull MessageType messageType,
        @Size(max = 2000) String content,
        @Size(max = 500) String imageUrl
) {
}
