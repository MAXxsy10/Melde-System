package dhbw.studienarbeit.meldesystem.repository;


import dhbw.studienarbeit.meldesystem.model.Helper;
import dhbw.studienarbeit.meldesystem.model.Report;
import dhbw.studienarbeit.meldesystem.model.ReportHistory;
import dhbw.studienarbeit.meldesystem.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface HelperRepository extends JpaRepository<Helper, Long> {

    Boolean existsByReportAndUser(Report report, User user);

    List<Helper> findByReport(Report report);

    @Query("SELECT h.report FROM Helper h WHERE h.user = :user")
    Page<Report> findReportsByUser(@Param("user") User user, Pageable pageable);

    Optional<Helper> findByReportAndUser(Report report, User user);

    Integer countByReport(Report report);
}


