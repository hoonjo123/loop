package world.loop.domain.user.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import world.loop.domain.user.dto.req.NicknameConfigureRequest;
import world.loop.domain.user.dto.res.NicknameConfigureResponse;
import world.loop.domain.user.service.UserService;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PutMapping("/me/nickname")
    public NicknameConfigureResponse configureNickname(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody NicknameConfigureRequest request
    ) {
        String nickname = userService.configureNickname(userId, request.nickname());
        return new NicknameConfigureResponse(nickname, true);
    }
}
