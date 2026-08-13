package world.loop.domain.chat.websocket;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import world.loop.domain.chat.dto.req.ChatMessageSendRequest;
import world.loop.domain.chat.dto.res.ChatMessageResponse;
import world.loop.domain.chat.service.ChatMessageService;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat/rooms/{roomId}/messages")
    public void send(
            @DestinationVariable Long roomId,
            @Valid ChatMessageSendRequest request,
            SimpMessageHeaderAccessor headers
    ) {
        Long userId = (Long) headers.getSessionAttributes()
                .get(ChatWebSocketHandshakeInterceptor.USER_ID_ATTRIBUTE);
        ChatMessageResponse response = chatMessageService.send(userId, roomId, request);
        messagingTemplate.convertAndSend("/topic/chat/rooms/" + roomId, response);
    }
}
