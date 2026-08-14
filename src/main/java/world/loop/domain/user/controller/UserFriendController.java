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
import world.loop.domain.user.dto.res.FriendResponse;
import world.loop.domain.user.service.UserFriendService;

@RestController
@RequestMapping("/api/users/me/friends")
@RequiredArgsConstructor
public class UserFriendController {

    private final UserFriendService userFriendService;

    @PostMapping("/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void add(@AuthenticationPrincipal Long userId, @PathVariable Long targetUserId) {
        userFriendService.add(userId, targetUserId);
    }

    @DeleteMapping("/{targetUserId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@AuthenticationPrincipal Long userId, @PathVariable Long targetUserId) {
        userFriendService.remove(userId, targetUserId);
    }

    @GetMapping
    public List<FriendResponse> getFriends(@AuthenticationPrincipal Long userId) {
        return userFriendService.getFriends(userId);
    }
}
