package world.loop.domain.chat.dto.req;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import world.loop.domain.chat.entity.OpenChatType;

public record ChatRoomCreateRequest(
        @NotNull OpenChatType openChatType,
        @NotBlank @Size(max = 40) String title,
        @NotBlank @Size(max = 120) String description,
        @NotBlank @Size(max = 100) String regionLabel,
        @NotNull @DecimalMin("-90.0") @DecimalMax("90.0") BigDecimal latitude,
        @NotNull @DecimalMin("-180.0") @DecimalMax("180.0") BigDecimal longitude
) {
}
