package dhbw.studienarbeit.meldesystem.repository;

import dhbw.studienarbeit.meldesystem.model.ChatMessage;
import dhbw.studienarbeit.meldesystem.model.Report;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    // Alle Nachrichten eines Reports
    List<ChatMessage> findByReportOrderBySentAtAsc(Report report);

    // Nachrichten eines Reports mit Paginierung
    Page<ChatMessage> findByReportOrderBySentAtDesc(Report report, Pageable pageable);

    // Neueste Nachrichten seit einem Zeitpunkt
    @Query("SELECT cm FROM ChatMessage cm WHERE cm.report = :report " +
            "AND cm.sentAt > :since ORDER BY cm.sentAt ASC")
    List<ChatMessage> findRecentMessages(
            @Param("report") Report report,
            @Param("since") java.time.LocalDateTime since
    );
}

