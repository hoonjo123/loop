package world.loop.domain.chat.repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import world.loop.domain.chat.entity.ChatRoom;
import world.loop.domain.chat.entity.ChatRoomStatus;
import world.loop.domain.chat.entity.ChatRoomType;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findByRoomTypeAndStatusOrderByCreatedAtDesc(
            ChatRoomType roomType,
            ChatRoomStatus status
    );

    List<ChatRoom> findByRoomTypeAndStatusAndRegionLabelContainingOrderByCreatedAtDesc(
            ChatRoomType roomType,
            ChatRoomStatus status,
            String regionLabel
    );

    Optional<ChatRoom> findByDirectKey(String directKey);

    List<ChatRoom> findByStatusAndExpiresAtLessThanEqual(
            ChatRoomStatus status,
            LocalDateTime expiresAt
    );

    @Query("""
            select room from ChatRoom room
            join fetch room.createdBy
            where room.id = :roomId
            """)
    Optional<ChatRoom> findDetailById(@Param("roomId") Long roomId);
}
