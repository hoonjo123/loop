package world.loop.domain.report.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import world.loop.domain.report.dto.req.ReportCreateRequest;
import world.loop.domain.report.dto.res.ReportCreateResponse;
import world.loop.domain.report.service.ReportService;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
public class ReportController {

    private final ReportService reportService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ReportCreateResponse create(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody ReportCreateRequest request
    ) {
        return reportService.create(userId, request);
    }
}
