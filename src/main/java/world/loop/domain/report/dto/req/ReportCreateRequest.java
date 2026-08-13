package world.loop.domain.report.dto.req;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ReportCreateRequest(
        Long reportedUserId,
        Long reportedRoomId,
        Long reportedMessageId,
        @NotBlank @Size(max = 50) String reason,
        @Size(max = 1000) String description
) {
}
