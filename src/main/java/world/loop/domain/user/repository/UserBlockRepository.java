package world.loop.domain.user.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import world.loop.domain.user.entity.UserBlock;

public interface UserBlockRepository extends JpaRepository<UserBlock, Long> {

    Optional<UserBlock> findByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    boolean existsByBlockerIdAndBlockedId(Long blockerId, Long blockedId);

    @Query("""
            select count(block) > 0 from UserBlock block
            where (block.blocker.id = :firstId and block.blocked.id = :secondId)
               or (block.blocker.id = :secondId and block.blocked.id = :firstId)
            """)
    boolean existsBetween(@Param("firstId") Long firstId, @Param("secondId") Long secondId);

    @Query("""
            select block from UserBlock block
            join fetch block.blocked
            where block.blocker.id = :userId
            order by block.createdAt desc
            """)
    List<UserBlock> findAllByBlockerId(@Param("userId") Long userId);
}
