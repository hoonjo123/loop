package world.loop.domain.chat.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import world.loop.domain.chat.entity.ChatMessage;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
            select message from ChatMessage message
            left join fetch message.sender
            where message.room.id = :roomId
              and (:beforeId is null or message.id < :beforeId)
            order by message.id desc
            """)
    List<ChatMessage> findPage(
            @Param("roomId") Long roomId,
            @Param("beforeId") Long beforeId,
            Pageable pageable
    );

    @Query("""
            select message from ChatMessage message
            left join fetch message.sender
            where message.room.id = :roomId
              and message.deletedAt is null
              and lower(message.content) like lower(concat('%', :query, '%'))
            order by message.id desc
            """)
    List<ChatMessage> search(
            @Param("roomId") Long roomId,
            @Param("query") String query,
            Pageable pageable
    );

    Optional<ChatMessage> findTopByRoomIdOrderByIdDesc(Long roomId);

    long countByRoomIdAndCreatedAtAfterAndSenderIdNot(
            Long roomId,
            LocalDateTime createdAt,
            Long senderId
    );
}
