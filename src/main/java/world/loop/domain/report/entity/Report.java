package world.loop.domain.report.entity;

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
import world.loop.common.BaseTimeEntity;
import world.loop.domain.chat.entity.ChatMessage;
import world.loop.domain.chat.entity.ChatRoom;
import world.loop.domain.user.entity.User;

@Entity
@Table(name = "reports", indexes = {
        @Index(name = "idx_reports_status_created", columnList = "status,created_at"),
        @Index(name = "idx_reports_reported_user_created", columnList = "reported_user_id,created_at"),
        @Index(name = "idx_reports_reported_room_created", columnList = "reported_room_id,created_at"),
        @Index(name = "idx_reports_reported_message_created", columnList = "reported_message_id,created_at")
})
public class Report extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", nullable = false)
    private User reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_user_id")
    private User reportedUser;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_room_id")
    private ChatRoom reportedRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported_message_id")
    private ChatMessage reportedMessage;

    @Column(nullable = false, length = 50)
    private String reason;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReportStatus status = ReportStatus.PENDING;

    protected Report() {
    }
}
