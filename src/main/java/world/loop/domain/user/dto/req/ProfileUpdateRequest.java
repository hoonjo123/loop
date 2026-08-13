package world.loop.domain.user.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @NotBlank
        @Size(max = 12)
        String nickname,

        @Size(max = 100)
        String introduction,

        @Size(max = 100)
        String activityArea,

        @Size(max = 500)
        String profileImageUrl
) {
}
