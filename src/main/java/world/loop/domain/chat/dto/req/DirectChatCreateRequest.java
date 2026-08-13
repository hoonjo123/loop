package world.loop.domain.chat.dto.req;

import jakarta.validation.constraints.NotNull;

public record DirectChatCreateRequest(@NotNull Long targetUserId) {
}
