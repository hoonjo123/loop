package world.loop.domain.chat.controller;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import world.loop.domain.chat.dto.req.ChatRoomCreateRequest;
import world.loop.domain.chat.dto.req.DirectChatCreateRequest;
import world.loop.domain.chat.dto.res.ChatMemberResponse;
import world.loop.domain.chat.dto.res.ChatRoomResponse;
import world.loop.domain.chat.dto.res.ConversationResponse;
import world.loop.domain.chat.service.ChatRoomService;
import world.loop.domain.chat.service.ChatMessageService;
import org.springframework.messaging.simp.SimpMessagingTemplate;

@RestController
@RequestMapping("/api/chat-rooms")
@RequiredArgsConstructor
public class ChatRoomController {

    private final ChatRoomService chatRoomService;
    private final ChatMessageService chatMessageService;
    private final SimpMessagingTemplate messagingTemplate;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ChatRoomResponse create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ChatRoomCreateRequest request
    ) {
        return chatRoomService.createOpenRoom(userId, request);
    }

    @PostMapping("/direct")
    public ChatRoomResponse createDirect(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody DirectChatCreateRequest request
    ) {
        return chatRoomService.createDirectRoom(userId, request);
    }

    @GetMapping
    public List<ChatRoomResponse> findRooms(
            @AuthenticationPrincipal Long userId,
            @RequestParam(required = false) String region
    ) {
        return chatRoomService.findOpenRooms(userId, region);
    }

    @GetMapping("/{roomId}")
    public ChatRoomResponse getRoom(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long roomId
    ) {
        return chatRoomService.getRoom(userId, roomId);
    }

    @PostMapping("/{roomId}/members")
    public ChatRoomResponse join(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long roomId
    ) {
        boolean alreadyJoined = chatRoomService.isMember(roomId, userId);
        ChatRoomResponse room = chatRoomService.join(userId, roomId);
        if (!alreadyJoined && room.roomType() == world.loop.domain.chat.entity.ChatRoomType.OPEN) {
            publishSystemMessage(roomId, "새로운 참여자가 채팅방에 입장했습니다.");
        }
        return room;
    }

    @DeleteMapping("/{roomId}/members/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leave(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long roomId
    ) {
        chatRoomService.leave(userId, roomId);
        publishSystemMessage(roomId, "참여자 한 명이 채팅방을 나갔습니다.");
    }

    @DeleteMapping("/{roomId}/members/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void kick(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long roomId,
            @PathVariable Long targetUserId
    ) {
        chatRoomService.kick(userId, roomId, targetUserId);
        publishSystemMessage(roomId, "참여자 한 명이 방장에 의해 퇴장되었습니다.");
    }

    @DeleteMapping("/{roomId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void close(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long roomId
    ) {
        chatRoomService.close(userId, roomId);
    }

    @GetMapping("/{roomId}/members")
    public List<ChatMemberResponse> getMembers(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long roomId
    ) {
        return chatRoomService.getMembers(userId, roomId);
    }

    @GetMapping("/me/conversations")
    public List<ConversationResponse> getMyConversations(@AuthenticationPrincipal Long userId) {
        return chatRoomService.getMyConversations(userId);
    }

    @PutMapping("/{roomId}/read")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markRead(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long roomId
    ) {
        chatRoomService.markRead(userId, roomId);
    }

    private void publishSystemMessage(Long roomId, String content) {
        messagingTemplate.convertAndSend(
                "/topic/chat/rooms/" + roomId,
                chatMessageService.sendSystem(roomId, content)
        );
    }
}
