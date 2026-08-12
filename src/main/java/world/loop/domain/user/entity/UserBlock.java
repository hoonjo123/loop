package world.loop.domain.user.entity;

import jakarta.persistence.Entity;
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

@Entity
@Table(name = "user_blocks",
        uniqueConstraints = @UniqueConstraint(name = "uk_user_blocks_blocker_blocked", columnNames = {"blocker_id", "blocked_id"}),
        indexes = @Index(name = "idx_user_blocks_blocked", columnList = "blocked_id"))
public class UserBlock extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocker_id", nullable = false)
    private User blocker;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "blocked_id", nullable = false)
    private User blocked;

    protected UserBlock() {
    }
}
