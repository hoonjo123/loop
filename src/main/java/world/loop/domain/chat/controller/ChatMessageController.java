package world.loop.domain.chat.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import world.loop.domain.chat.dto.res.ChatMessagePageResponse;
import world.loop.domain.chat.dto.res.ChatMessageResponse;
import world.loop.domain.chat.service.ChatMessageService;

@RestController
@RequestMapping("/api/chat-rooms/{roomId}/messages")
@RequiredArgsConstructor
public class ChatMessageController {

    private final ChatMessageService chatMessageService;

    @GetMapping
    public ChatMessagePageResponse getMessages(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long roomId,
            @RequestParam(required = false) Long beforeId,
            @RequestParam(defaultValue = "50") int size
    ) {
        return chatMessageService.getMessages(userId, roomId, beforeId, size);
    }

    @GetMapping("/search")
    public List<ChatMessageResponse> search(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long roomId,
            @RequestParam String query
    ) {
        return chatMessageService.search(userId, roomId, query);
    }

    @DeleteMapping("/{messageId}")
    public ChatMessageResponse delete(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long roomId,
            @PathVariable Long messageId
    ) {
        return chatMessageService.delete(userId, roomId, messageId);
    }
}
