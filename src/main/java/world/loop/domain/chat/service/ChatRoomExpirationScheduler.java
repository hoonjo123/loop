package world.loop.domain.chat.service;

import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import world.loop.domain.chat.entity.ChatRoomStatus;
import world.loop.domain.chat.repository.ChatRoomRepository;

@Component
@RequiredArgsConstructor
public class ChatRoomExpirationScheduler {

    private final ChatRoomRepository chatRoomRepository;

    @Scheduled(fixedDelay = 60_000)
    @Transactional
    public void closeExpiredRooms() {
        chatRoomRepository.findByStatusAndExpiresAtLessThanEqual(
                ChatRoomStatus.ACTIVE,
                LocalDateTime.now()
        ).forEach(room -> room.close());
    }
}
