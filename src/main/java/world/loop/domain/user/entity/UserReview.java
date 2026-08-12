package world.loop.domain.user.entity;

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
import world.loop.common.BaseTimeEntity;
import world.loop.domain.chat.entity.ChatRoom;

@Entity
@Table(name = "user_reviews",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_reviews_reviewer_reviewee_room", columnNames = {"reviewer_id", "reviewee_id", "room_id"}),
        indexes = @Index(name = "idx_user_reviews_reviewee_created", columnList = "reviewee_id,created_at"))
public class UserReview extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id", nullable = false)
    private User reviewee;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private ChatRoom room;

    @Enumerated(EnumType.STRING)
    @Column(name = "review_type", nullable = false, length = 20)
    private ReviewType reviewType;

    @Column(nullable = false, length = 50)
    private String reason;

    @Column(name = "invalidated", nullable = false)
    private boolean invalidated;

    protected UserReview() {
    }
}
