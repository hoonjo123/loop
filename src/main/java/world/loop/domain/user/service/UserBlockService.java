package world.loop.domain.user.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.loop.domain.user.dto.res.BlockedUserResponse;
import world.loop.domain.user.entity.User;
import world.loop.domain.user.entity.UserBlock;
import world.loop.domain.user.repository.UserBlockRepository;
import world.loop.domain.user.repository.UserRepository;
import world.loop.global.exception.BusinessException;
import world.loop.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class UserBlockService {

    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;

    @Transactional
    public void block(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (userBlockRepository.existsByBlockerIdAndBlockedId(userId, targetUserId)) {
            return;
        }
        User blocker = findUser(userId);
        User blocked = findUser(targetUserId);
        userBlockRepository.save(UserBlock.builder()
                .blocker(blocker)
                .blocked(blocked)
                .build());
    }

    @Transactional
    public void unblock(Long userId, Long targetUserId) {
        userBlockRepository.findByBlockerIdAndBlockedId(userId, targetUserId)
                .ifPresent(userBlockRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<BlockedUserResponse> getBlockedUsers(Long userId) {
        return userBlockRepository.findAllByBlockerId(userId).stream()
                .map(BlockedUserResponse::from)
                .toList();
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
