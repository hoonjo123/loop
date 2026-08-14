package world.loop.domain.user.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import world.loop.domain.user.entity.UserFriend;

public interface UserFriendRepository extends JpaRepository<UserFriend, Long> {

    Optional<UserFriend> findByUserOneIdAndUserTwoId(Long userOneId, Long userTwoId);

    boolean existsByUserOneIdAndUserTwoId(Long userOneId, Long userTwoId);

    @Query("""
            select friend from UserFriend friend
            join fetch friend.userOne
            join fetch friend.userTwo
            where friend.userOne.id = :userId or friend.userTwo.id = :userId
            order by friend.createdAt desc
            """)
    List<UserFriend> findAllByUserId(@Param("userId") Long userId);
}
