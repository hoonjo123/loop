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
import java.time.LocalDateTime;
import world.loop.common.BaseTimeEntity;
import world.loop.domain.user.entity.User;

@Entity
@Table(name = "chat_rooms", indexes = {
        @Index(name = "idx_chat_rooms_region_status", columnList = "region_label,status"),
        @Index(name = "idx_chat_rooms_expires_at", columnList = "expires_at")
})
public class ChatRoom extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "room_type", nullable = false, length = 20)
    private ChatRoomType roomType;

    @Enumerated(EnumType.STRING)
    @Column(name = "duration_type", length = 20)
    private RoomDurationType durationType;

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

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChatRoomStatus status = ChatRoomStatus.ACTIVE;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_id", nullable = false)
    private User createdBy;

    protected ChatRoom() {
    }
}
