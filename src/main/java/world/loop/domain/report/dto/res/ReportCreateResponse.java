package world.loop.domain.report.dto.res;

import world.loop.domain.report.entity.Report;
import world.loop.domain.report.entity.ReportStatus;

public record ReportCreateResponse(Long id, ReportStatus status) {

    public static ReportCreateResponse from(Report report) {
        return new ReportCreateResponse(report.getId(), report.getStatus());
    }
}
