package world.loop.domain.auth.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record NicknameCheckRequest(
        @NotBlank @Size(max = 12) String nickname
) {
}
