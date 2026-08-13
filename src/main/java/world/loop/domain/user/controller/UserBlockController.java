package world.loop.domain.user.controller;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import world.loop.domain.user.dto.res.BlockedUserResponse;
import world.loop.domain.user.service.UserBlockService;

@RestController
@RequestMapping("/api/users/me/blocks")
@RequiredArgsConstructor
public class UserBlockController {

    private final UserBlockService userBlockService;

    @PostMapping("/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void block(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long targetUserId
    ) {
        userBlockService.block(userId, targetUserId);
    }

    @DeleteMapping("/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unblock(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long targetUserId
    ) {
        userBlockService.unblock(userId, targetUserId);
    }

    @GetMapping
    public List<BlockedUserResponse> getBlockedUsers(@AuthenticationPrincipal Long userId) {
        return userBlockService.getBlockedUsers(userId);
    }
}
