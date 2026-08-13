package world.loop.domain.user.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import world.loop.domain.user.dto.req.ProfileUpdateRequest;
import world.loop.domain.user.dto.res.ProfileResponse;
import world.loop.domain.user.entity.AuthProvider;
import world.loop.domain.user.entity.User;
import world.loop.domain.user.repository.UserRepository;
import world.loop.global.exception.BusinessException;
import world.loop.global.exception.ErrorCode;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    @Test
    void updatesMyProfile() {
        User user = user();
        ProfileUpdateRequest request = new ProfileUpdateRequest(
                "새닉네임",
                " 동네 친구를 찾고 있어요. ",
                " 서울 마포구 ",
                " https://example.com/profile.png "
        );
        given(userRepository.findById(1L)).willReturn(Optional.of(user));
        given(userRepository.existsByNicknameAndIdNot("새닉네임", 1L)).willReturn(false);

        ProfileResponse response = userService.updateMyProfile(1L, request);

        assertThat(response.nickname()).isEqualTo("새닉네임");
        assertThat(response.introduction()).isEqualTo("동네 친구를 찾고 있어요.");
        assertThat(response.activityArea()).isEqualTo("서울 마포구");
        assertThat(response.profileImageUrl()).isEqualTo("https://example.com/profile.png");
    }

    @Test
    void rejectsDuplicateNickname() {
        ProfileUpdateRequest request = new ProfileUpdateRequest("중복닉네임", null, null, null);
        given(userRepository.findById(1L)).willReturn(Optional.of(user()));
        given(userRepository.existsByNicknameAndIdNot("중복닉네임", 1L)).willReturn(true);

        assertThatThrownBy(() -> userService.updateMyProfile(1L, request))
                .isInstanceOfSatisfying(BusinessException.class, exception ->
                        assertThat(exception.getErrorCode()).isEqualTo(ErrorCode.NICKNAME_ALREADY_EXISTS));
    }

    private User user() {
        return User.builder()
                .email("user@example.com")
                .nickname("기존닉네임")
                .nicknameConfigured(true)
                .authProvider(AuthProvider.LOCAL)
                .build();
    }
}
