package world.loop.domain.chat.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import world.loop.domain.chat.entity.ChatRoomMember;

public interface ChatRoomMemberRepository extends JpaRepository<ChatRoomMember, Long> {

    Optional<ChatRoomMember> findByRoomIdAndUserId(Long roomId, Long userId);

    boolean existsByRoomIdAndUserIdAndLeftAtIsNull(Long roomId, Long userId);

    long countByRoomIdAndLeftAtIsNull(Long roomId);

    @Query("""
            select member from ChatRoomMember member
            join fetch member.user
            where member.room.id = :roomId and member.leftAt is null
            order by member.joinedAt
            """)
    List<ChatRoomMember> findActiveMembers(@Param("roomId") Long roomId);

    @Query("""
            select member from ChatRoomMember member
            join fetch member.room room
            where member.user.id = :userId and member.leftAt is null
            order by room.updatedAt desc
            """)
    List<ChatRoomMember> findActiveRoomsByUserId(@Param("userId") Long userId);
}
