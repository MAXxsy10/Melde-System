package dhbw.studienarbeit.meldesystem.repository;

import dhbw.studienarbeit.meldesystem.model.Report;
import dhbw.studienarbeit.meldesystem.model.ReportHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReportHistoryRepository extends JpaRepository<ReportHistory, Long> {

    // Historie eines Reports sortiert nach Timestamp
    List<ReportHistory> findByReportOrderByTimestampDesc(Report report);

    // Letzter Status-Eintrag
    @Query("SELECT rh FROM ReportHistory rh WHERE rh.report = :report " +
            "ORDER BY rh.timestamp DESC LIMIT 1")
    Optional<ReportHistory> findLatestByReport(@Param("report") Report report);
}

