package world.loop.domain.chat.websocket;

import java.time.Instant;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;
import world.loop.domain.auth.token.AccessTokenRevocationService;
import world.loop.domain.chat.service.ChatRoomService;

@Component
@RequiredArgsConstructor
public class ChatWebSocketAuthorizationInterceptor implements ChannelInterceptor {

    private final AccessTokenRevocationService accessTokenRevocationService;
    private final ChatRoomService chatRoomService;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);
        if (accessor.getCommand() == StompCommand.SEND
                || accessor.getCommand() == StompCommand.SUBSCRIBE) {
            validateSession(accessor.getSessionAttributes());
        }
        if (accessor.getCommand() == StompCommand.SUBSCRIBE) {
            validateSubscription(accessor.getDestination(), accessor.getSessionAttributes());
        }
        return message;
    }

    private void validateSession(Map<String, Object> attributes) {
        if (attributes == null) {
            throw new MessagingException("WebSocket session is not authenticated.");
        }
        Object expirationValue = attributes.get(ChatWebSocketHandshakeInterceptor.TOKEN_EXPIRATION_ATTRIBUTE);
        Object tokenIdValue = attributes.get(ChatWebSocketHandshakeInterceptor.TOKEN_ID_ATTRIBUTE);
        if (!(expirationValue instanceof Instant expiration)
                || !(tokenIdValue instanceof String tokenId)
                || !expiration.isAfter(Instant.now())
                || accessTokenRevocationService.isRevoked(tokenId)) {
            throw new MessagingException("WebSocket authentication has expired.");
        }
    }

    private void validateSubscription(String destination, Map<String, Object> attributes) {
        String prefix = "/topic/chat/rooms/";
        if (destination == null || !destination.startsWith(prefix)) {
            return;
        }
        try {
            Long roomId = Long.valueOf(destination.substring(prefix.length()));
            Long userId = (Long) attributes.get(ChatWebSocketHandshakeInterceptor.USER_ID_ATTRIBUTE);
            chatRoomService.requireMembership(roomId, userId);
        } catch (NumberFormatException exception) {
            throw new MessagingException("Invalid chat room destination.");
        }
    }
}
