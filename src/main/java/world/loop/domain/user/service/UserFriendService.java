package world.loop.domain.user.service;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.loop.domain.user.dto.res.FriendResponse;
import world.loop.domain.user.entity.User;
import world.loop.domain.user.entity.UserFriend;
import world.loop.domain.user.repository.UserBlockRepository;
import world.loop.domain.user.repository.UserFriendRepository;
import world.loop.domain.user.repository.UserRepository;
import world.loop.global.exception.BusinessException;
import world.loop.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class UserFriendService {

    private final UserFriendRepository userFriendRepository;
    private final UserBlockRepository userBlockRepository;
    private final UserRepository userRepository;

    @Transactional
    public void add(Long userId, Long targetUserId) {
        if (userId.equals(targetUserId)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST);
        }
        if (userBlockRepository.existsBetween(userId, targetUserId)) {
            throw new BusinessException(ErrorCode.BLOCKED_USER_INTERACTION);
        }
        Long userOneId = Math.min(userId, targetUserId);
        Long userTwoId = Math.max(userId, targetUserId);
        if (userFriendRepository.existsByUserOneIdAndUserTwoId(userOneId, userTwoId)) {
            return;
        }
        User userOne = findUser(userOneId);
        User userTwo = findUser(userTwoId);
        userFriendRepository.save(UserFriend.builder()
                .userOne(userOne)
                .userTwo(userTwo)
                .build());
    }

    @Transactional
    public void remove(Long userId, Long targetUserId) {
        Long userOneId = Math.min(userId, targetUserId);
        Long userTwoId = Math.max(userId, targetUserId);
        userFriendRepository.findByUserOneIdAndUserTwoId(userOneId, userTwoId)
                .ifPresent(userFriendRepository::delete);
    }

    @Transactional(readOnly = true)
    public List<FriendResponse> getFriends(Long userId) {
        return userFriendRepository.findAllByUserId(userId).stream()
                .map(friend -> FriendResponse.from(friend, userId))
                .toList();
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }
}
