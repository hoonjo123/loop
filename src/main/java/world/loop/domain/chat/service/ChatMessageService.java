package world.loop.domain.chat.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.loop.domain.chat.dto.req.ChatMessageSendRequest;
import world.loop.domain.chat.dto.res.ChatMessagePageResponse;
import world.loop.domain.chat.dto.res.ChatMessageResponse;
import world.loop.domain.chat.entity.ChatMessage;
import world.loop.domain.chat.entity.ChatRoom;
import world.loop.domain.chat.entity.MessageType;
import world.loop.domain.chat.repository.ChatMessageRepository;
import world.loop.domain.user.entity.User;
import world.loop.domain.user.repository.UserRepository;
import world.loop.global.exception.BusinessException;
import world.loop.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class ChatMessageService {

    private static final int MAX_PAGE_SIZE = 100;

    private final ChatMessageRepository chatMessageRepository;
    private final ChatRoomService chatRoomService;
    private final UserRepository userRepository;

    @Transactional
    public ChatMessageResponse send(Long userId, Long roomId, ChatMessageSendRequest request) {
        chatRoomService.requireMessageAccess(roomId, userId);
        ChatRoom room = chatRoomService.findActiveRoom(roomId);
        chatRoomService.requireDirectMessageAllowed(room, userId);
        User sender = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        validateMessage(request);
        ChatMessage message = chatMessageRepository.save(ChatMessage.builder()
                .room(room)
                .sender(sender)
                .messageType(request.messageType())
                .content(normalize(request.content()))
                .imageUrl(normalize(request.imageUrl()))
                .build());
        return ChatMessageResponse.from(message);
    }

    @Transactional
    public ChatMessageResponse sendSystem(Long roomId, String content) {
        ChatRoom room = chatRoomService.findActiveRoom(roomId);
        ChatMessage message = chatMessageRepository.save(ChatMessage.builder()
                .room(room)
                .messageType(MessageType.SYSTEM)
                .content(content)
                .build());
        return ChatMessageResponse.from(message);
    }

    @Transactional(readOnly = true)
    public ChatMessagePageResponse getMessages(Long userId, Long roomId, Long beforeId, int size) {
        chatRoomService.requireMessageAccess(roomId, userId);
        int pageSize = Math.min(Math.max(size, 1), MAX_PAGE_SIZE);
        List<ChatMessage> fetched = chatMessageRepository.findPage(
                roomId,
                beforeId,
                PageRequest.of(0, pageSize + 1)
        );
        boolean hasNext = fetched.size() > pageSize;
        List<ChatMessage> page = new ArrayList<>(fetched.subList(0, Math.min(pageSize, fetched.size())));
        Long nextCursor = hasNext ? page.get(page.size() - 1).getId() : null;
        Collections.reverse(page);
        return new ChatMessagePageResponse(
                page.stream().map(ChatMessageResponse::from).toList(),
                nextCursor,
                hasNext
        );
    }

    @Transactional(readOnly = true)
    public List<ChatMessageResponse> search(Long userId, Long roomId, String query) {
        chatRoomService.requireMessageAccess(roomId, userId);
        if (query == null || query.isBlank()) {
            return List.of();
        }
        return chatMessageRepository.search(roomId, query.trim(), PageRequest.of(0, 100)).stream()
                .map(ChatMessageResponse::from)
                .toList();
    }

    @Transactional
    public ChatMessageResponse delete(Long userId, Long roomId, Long messageId) {
        chatRoomService.requireMessageAccess(roomId, userId);
        ChatMessage message = chatMessageRepository.findById(messageId)
                .filter(item -> item.getRoom().getId().equals(roomId))
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MESSAGE_NOT_FOUND));
        if (message.getSender() == null || !message.getSender().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.CHAT_MESSAGE_DELETE_DENIED);
        }
        message.delete();
        return ChatMessageResponse.from(message);
    }

    private void validateMessage(ChatMessageSendRequest request) {
        boolean textInvalid = request.messageType() == MessageType.TEXT
                && (request.content() == null || request.content().isBlank());
        boolean imageInvalid = request.messageType() == MessageType.IMAGE
                && (request.imageUrl() == null || request.imageUrl().isBlank());
        if (request.messageType() == MessageType.SYSTEM || textInvalid || imageInvalid) {
            throw new BusinessException(ErrorCode.INVALID_CHAT_MESSAGE);
        }
    }

    private String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
