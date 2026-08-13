package world.loop.domain.chat.dto.res;

import java.util.List;

public record ChatMessagePageResponse(
        List<ChatMessageResponse> messages,
        Long nextCursor,
        boolean hasNext
) {
}
