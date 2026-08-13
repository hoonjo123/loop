package world.loop.domain.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NicknameConfigureRequest(
        @NotBlank @Size(max = 12) String nickname
) {
}
