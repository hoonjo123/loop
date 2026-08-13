package world.loop.domain.report.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import world.loop.domain.report.entity.Report;

public interface ReportRepository extends JpaRepository<Report, Long> {
}
