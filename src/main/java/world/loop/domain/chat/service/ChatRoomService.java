package world.loop.domain.chat.service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.loop.domain.chat.dto.req.ChatRoomCreateRequest;
import world.loop.domain.chat.dto.req.DirectChatCreateRequest;
import world.loop.domain.chat.dto.res.ChatMemberResponse;
import world.loop.domain.chat.dto.res.ChatRoomResponse;
import world.loop.domain.chat.dto.res.ConversationResponse;
import world.loop.domain.chat.entity.ChatMemberRole;
import world.loop.domain.chat.entity.ChatMessage;
import world.loop.domain.chat.entity.ChatRoom;
import world.loop.domain.chat.entity.ChatRoomMember;
import world.loop.domain.chat.entity.ChatRoomStatus;
import world.loop.domain.chat.entity.ChatRoomType;
import world.loop.domain.chat.entity.RoomDurationType;
import world.loop.domain.chat.repository.ChatMessageRepository;
import world.loop.domain.chat.repository.ChatRoomMemberRepository;
import world.loop.domain.chat.repository.ChatRoomRepository;
import world.loop.domain.user.entity.User;
import world.loop.domain.user.repository.UserRepository;
import world.loop.domain.user.repository.UserBlockRepository;
import world.loop.global.exception.BusinessException;
import world.loop.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomMemberRepository chatRoomMemberRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final UserRepository userRepository;
    private final UserBlockRepository userBlockRepository;

    @Transactional
    public ChatRoomResponse createOpenRoom(Long userId, ChatRoomCreateRequest request) {
        validateExpiration(request);
        User owner = findUser(userId);
        ChatRoom room = chatRoomRepository.save(ChatRoom.builder()
                .roomType(ChatRoomType.OPEN)
                .durationType(request.durationType())
                .title(request.title().trim())
                .description(request.description().trim())
                .regionLabel(request.regionLabel().trim())
                .latitude(request.latitude())
                .longitude(request.longitude())
                .expiresAt(request.durationType() == RoomDurationType.TEMPORARY ? request.expiresAt() : null)
                .createdBy(owner)
                .build());
        chatRoomMemberRepository.save(ChatRoomMember.builder()
                .room(room)
                .user(owner)
                .role(ChatMemberRole.OWNER)
                .build());
        return ChatRoomResponse.of(room, 1, true);
    }

    @Transactional
    public ChatRoomResponse createDirectRoom(Long userId, DirectChatCreateRequest request) {
        if (userId.equals(request.targetUserId())) {
            throw new BusinessException(ErrorCode.DIRECT_CHAT_SELF_NOT_ALLOWED);
        }
        User requester = findUser(userId);
        User target = findUser(request.targetUserId());
        requireNoBlockBetween(userId, target.getId());
        String directKey = directKey(userId, target.getId());
        ChatRoom room = chatRoomRepository.findByDirectKey(directKey)
                .orElseGet(() -> createDirectRoom(requester, target, directKey));
        restoreDirectMembership(room, requester);
        restoreDirectMembership(room, target);
        return ChatRoomResponse.of(room, 2, true);
    }

    @Transactional
    public List<ChatRoomResponse> findOpenRooms(Long userId, String region) {
        List<ChatRoom> rooms = region == null || region.isBlank()
                ? chatRoomRepository.findByRoomTypeAndStatusOrderByCreatedAtDesc(
                        ChatRoomType.OPEN,
                        ChatRoomStatus.ACTIVE
                )
                : chatRoomRepository.findByRoomTypeAndStatusAndRegionLabelContainingOrderByCreatedAtDesc(
                        ChatRoomType.OPEN,
                        ChatRoomStatus.ACTIVE,
                        region.trim()
                );
        LocalDateTime now = LocalDateTime.now();
        return rooms.stream()
                .filter(room -> !closeIfExpired(room, now))
                .map(room -> response(room, userId))
                .toList();
    }

    @Transactional
    public ChatRoomResponse getRoom(Long userId, Long roomId) {
        ChatRoom room = findActiveRoom(roomId);
        return response(room, userId);
    }

    @Transactional
    public ChatRoomResponse join(Long userId, Long roomId) {
        ChatRoom room = findActiveRoom(roomId);
        if (room.getRoomType() != ChatRoomType.OPEN) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
        User user = findUser(userId);
        ChatRoomMember member = chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .orElseGet(() -> chatRoomMemberRepository.save(ChatRoomMember.builder()
                        .room(room)
                        .user(user)
                        .role(ChatMemberRole.MEMBER)
                        .build()));
        if (!member.isActive()) {
            member.rejoin();
        }
        return response(room, userId);
    }

    @Transactional
    public void leave(Long userId, Long roomId) {
        ChatRoomMember member = findActiveMember(roomId, userId);
        if (member.getRole() == ChatMemberRole.OWNER) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_OWNER_CANNOT_LEAVE);
        }
        member.leave();
    }

    @Transactional
    public void kick(Long ownerId, Long roomId, Long targetUserId) {
        ChatRoomMember owner = findActiveMember(roomId, ownerId);
        if (owner.getRole() != ChatMemberRole.OWNER) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_OWNER_REQUIRED);
        }
        ChatRoomMember target = findActiveMember(roomId, targetUserId);
        if (target.getRole() == ChatMemberRole.OWNER) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_OWNER_REQUIRED);
        }
        target.leave();
    }

    @Transactional
    public void close(Long ownerId, Long roomId) {
        ChatRoomMember owner = findActiveMember(roomId, ownerId);
        if (owner.getRole() != ChatMemberRole.OWNER) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_OWNER_REQUIRED);
        }
        owner.getRoom().close();
    }

    @Transactional(readOnly = true)
    public List<ChatMemberResponse> getMembers(Long userId, Long roomId) {
        requireMembership(roomId, userId);
        return chatRoomMemberRepository.findActiveMembers(roomId).stream()
                .map(ChatMemberResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ConversationResponse> getMyConversations(Long userId) {
        return chatRoomMemberRepository.findActiveRoomsByUserId(userId).stream()
                .map(member -> conversation(member.getRoom(), member, userId))
                .sorted(Comparator.comparing(
                        ConversationResponse::lastMessageAt,
                        Comparator.nullsLast(Comparator.reverseOrder())
                ))
                .toList();
    }

    @Transactional
    public void markRead(Long userId, Long roomId) {
        findActiveMember(roomId, userId).markRead(LocalDateTime.now());
    }

    @Transactional(readOnly = true)
    public void requireMembership(Long roomId, Long userId) {
        if (!chatRoomMemberRepository.existsByRoomIdAndUserIdAndLeftAtIsNull(roomId, userId)) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_ACCESS_DENIED);
        }
    }

    @Transactional(readOnly = true)
    public boolean isMember(Long roomId, Long userId) {
        return chatRoomMemberRepository.existsByRoomIdAndUserIdAndLeftAtIsNull(roomId, userId);
    }

    @Transactional(readOnly = true)
    public void requireDirectMessageAllowed(ChatRoom room, Long userId) {
        if (room.getRoomType() != ChatRoomType.DIRECT) {
            return;
        }
        chatRoomMemberRepository.findActiveMembers(room.getId()).stream()
                .map(ChatRoomMember::getUser)
                .map(User::getId)
                .filter(memberId -> !memberId.equals(userId))
                .findFirst()
                .ifPresent(targetUserId -> requireNoBlockBetween(userId, targetUserId));
    }

    @Transactional
    public ChatRoom findActiveRoom(Long roomId) {
        ChatRoom room = chatRoomRepository.findDetailById(roomId)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_ROOM_NOT_FOUND));
        if (room.getStatus() != ChatRoomStatus.ACTIVE || closeIfExpired(room, LocalDateTime.now())) {
            throw new BusinessException(ErrorCode.CHAT_ROOM_CLOSED);
        }
        return room;
    }

    private ChatRoom createDirectRoom(User requester, User target, String directKey) {
        ChatRoom room = chatRoomRepository.save(ChatRoom.builder()
                .roomType(ChatRoomType.DIRECT)
                .directKey(directKey)
                .createdBy(requester)
                .build());
        chatRoomMemberRepository.save(ChatRoomMember.builder()
                .room(room)
                .user(requester)
                .role(ChatMemberRole.MEMBER)
                .build());
        chatRoomMemberRepository.save(ChatRoomMember.builder()
                .room(room)
                .user(target)
                .role(ChatMemberRole.MEMBER)
                .build());
        return room;
    }

    private void restoreDirectMembership(ChatRoom room, User user) {
        chatRoomMemberRepository.findByRoomIdAndUserId(room.getId(), user.getId())
                .ifPresent(member -> {
                    if (!member.isActive()) {
                        member.rejoin();
                    }
                });
    }

    private ConversationResponse conversation(ChatRoom room, ChatRoomMember membership, Long userId) {
        ChatMessage lastMessage = chatMessageRepository.findTopByRoomIdOrderByIdDesc(room.getId()).orElse(null);
        ChatRoomMember target = room.getRoomType() == ChatRoomType.DIRECT
                ? chatRoomMemberRepository.findActiveMembers(room.getId()).stream()
                        .filter(member -> !member.getUser().getId().equals(userId))
                        .findFirst()
                        .orElse(null)
                : null;
        LocalDateTime readAt = membership.getLastReadAt() == null
                ? membership.getJoinedAt()
                : membership.getLastReadAt();
        long unreadCount = chatMessageRepository.countByRoomIdAndCreatedAtAfterAndSenderIdNot(
                room.getId(),
                readAt,
                userId
        );
        return new ConversationResponse(
                room.getId(),
                room.getRoomType(),
                target == null ? room.getTitle() : target.getUser().getNickname(),
                room.getRoomType() == ChatRoomType.DIRECT ? "1:1 대화" : room.getRegionLabel(),
                chatRoomMemberRepository.countByRoomIdAndLeftAtIsNull(room.getId()),
                lastMessage == null ? "아직 메시지가 없습니다." : preview(lastMessage),
                lastMessage == null ? room.getCreatedAt() : lastMessage.getCreatedAt(),
                unreadCount,
                target == null ? null : target.getUser().getId(),
                target == null ? null : target.getUser().getProfileImageUrl()
        );
    }

    private ChatRoomResponse response(ChatRoom room, Long userId) {
        return ChatRoomResponse.of(
                room,
                chatRoomMemberRepository.countByRoomIdAndLeftAtIsNull(room.getId()),
                chatRoomMemberRepository.existsByRoomIdAndUserIdAndLeftAtIsNull(room.getId(), userId)
        );
    }

    private ChatRoomMember findActiveMember(Long roomId, Long userId) {
        return chatRoomMemberRepository.findByRoomIdAndUserId(roomId, userId)
                .filter(ChatRoomMember::isActive)
                .orElseThrow(() -> new BusinessException(ErrorCode.CHAT_MEMBER_NOT_FOUND));
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private boolean closeIfExpired(ChatRoom room, LocalDateTime now) {
        if (!room.isExpired(now)) {
            return false;
        }
        room.close();
        return true;
    }

    private void validateExpiration(ChatRoomCreateRequest request) {
        if (request.durationType() == RoomDurationType.TEMPORARY
                && (request.expiresAt() == null || !request.expiresAt().isAfter(LocalDateTime.now()))) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
    }

    private String directKey(Long firstUserId, Long secondUserId) {
        return Math.min(firstUserId, secondUserId) + ":" + Math.max(firstUserId, secondUserId);
    }

    private void requireNoBlockBetween(Long firstUserId, Long secondUserId) {
        if (userBlockRepository.existsBetween(firstUserId, secondUserId)) {
            throw new BusinessException(ErrorCode.BLOCKED_USER_INTERACTION);
        }
    }

    private String preview(ChatMessage message) {
        if (message.getDeletedAt() != null) {
            return "삭제된 메시지입니다.";
        }
        if (message.getMessageType() == world.loop.domain.chat.entity.MessageType.IMAGE) {
            return "사진을 보냈습니다.";
        }
        return message.getContent();
    }
}
