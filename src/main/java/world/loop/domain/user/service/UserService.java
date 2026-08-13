package world.loop.domain.user.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import world.loop.domain.user.entity.User;
import world.loop.domain.user.dto.req.ProfileUpdateRequest;
import world.loop.domain.user.dto.res.ProfileResponse;
import world.loop.domain.user.repository.UserRepository;
import world.loop.global.exception.BusinessException;
import world.loop.global.exception.ErrorCode;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public ProfileResponse getMyProfile(Long userId) {
        return ProfileResponse.from(findUser(userId));
    }

    @Transactional
    public ProfileResponse updateMyProfile(Long userId, ProfileUpdateRequest request) {
        User user = findUser(userId);
        String nickname = request.nickname().trim();
        if (userRepository.existsByNicknameAndIdNot(nickname, userId)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        user.updateProfile(
                nickname,
                normalize(request.introduction()),
                normalize(request.activityArea()),
                normalize(request.profileImageUrl())
        );
        return ProfileResponse.from(user);
    }

    @Transactional
    public String configureNickname(Long userId, String nickname) {
        User user = findUser(userId);
        if (user.isNicknameConfigured()) {
            throw new IllegalStateException("Nickname is already configured.");
        }
        if (userRepository.existsByNickname(nickname)) {
            throw new BusinessException(ErrorCode.NICKNAME_ALREADY_EXISTS);
        }
        user.configureNickname(nickname);
        return nickname;
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    private String normalize(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}
