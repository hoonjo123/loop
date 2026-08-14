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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import world.loop.common.BaseTimeEntity;

@Entity
@Table(name = "user_friends",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_user_friends_pair",
                columnNames = {"user_one_id", "user_two_id"}
        ),
        indexes = @Index(name = "idx_user_friends_user_two", columnList = "user_two_id"))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UserFriend extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_one_id", nullable = false)
    private User userOne;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_two_id", nullable = false)
    private User userTwo;

    @Builder
    private UserFriend(User userOne, User userTwo) {
        this.userOne = userOne;
        this.userTwo = userTwo;
    }
}
