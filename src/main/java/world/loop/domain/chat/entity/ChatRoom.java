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
import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import world.loop.common.BaseTimeEntity;
import world.loop.domain.user.entity.User;

@Entity
@Table(name = "chat_rooms", indexes = {
        @Index(name = "idx_chat_rooms_region_status", columnList = "region_label,status"),
        @Index(name = "idx_chat_rooms_open_chat_type", columnList = "open_chat_type"),
        @Index(name = "idx_chat_rooms_source_room_id", columnList = "source_room_id")
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private ChatRoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "open_chat_type", length = 20)
    private OpenChatType openChatType;

    @Column(length = 40)
    private String title;

    @Column(length = 120)
    private String description;

    @Column(name = "region_label", length = 100)
    private String regionLabel;

    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    @Column(name = "direct_key", unique = true, length = 50)
    private String directKey;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_room_id")
    private ChatRoom sourceRoom;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatRoomStatus status = ChatRoomStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    @Builder
    private ChatRoom(
            ChatRoomType roomType,
            OpenChatType openChatType,
            String title,
            String description,
            String regionLabel,
            BigDecimal latitude,
            BigDecimal longitude,
            String directKey,
            ChatRoom sourceRoom,
            User createdBy
    ) {
        this.roomType = roomType;
        this.openChatType = openChatType;
        this.title = title;
        this.description = description;
        this.regionLabel = regionLabel;
        this.latitude = latitude;
        this.longitude = longitude;
        this.directKey = directKey;
        this.sourceRoom = sourceRoom;
        this.createdBy = createdBy;
    }

    public boolean isOneToOneEntry() {
        return roomType == ChatRoomType.OPEN && openChatType == OpenChatType.ONE_TO_ONE;
    }

    public void close() {
        this.status = ChatRoomStatus.CLOSED;
    }
}
