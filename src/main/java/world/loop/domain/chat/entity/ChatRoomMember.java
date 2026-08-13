package world.loop.domain.chat.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import world.loop.common.BaseTimeEntity;
import world.loop.domain.user.entity.User;

@Entity
@Table(name = "chat_room_members",
        uniqueConstraints = @UniqueConstraint(name = "uk_chat_room_members_room_user", columnNames = {"room_id", "user_id"}),
        indexes = @Index(name = "idx_chat_room_members_user_joined", columnList = "user_id,joined_at"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoomMember extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatMemberRole role = ChatMemberRole.MEMBER;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(name = "last_read_at")
    private LocalDateTime lastReadAt;

    @Builder
    private ChatRoomMember(ChatRoom room, User user, ChatMemberRole role) {
        this.room = room;
        this.user = user;
        this.role = role;
        this.joinedAt = LocalDateTime.now();
    }

    public boolean isActive() {
        return leftAt == null;
    }

    public void rejoin() {
        this.joinedAt = LocalDateTime.now();
        this.leftAt = null;
    }

    public void leave() {
        this.leftAt = LocalDateTime.now();
    }

    public void markRead(LocalDateTime readAt) {
        this.lastReadAt = readAt;
    }
}
