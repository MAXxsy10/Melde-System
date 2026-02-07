package dhbw.studienarbeit.meldesystem.repository;


import dhbw.studienarbeit.meldesystem.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReportRepository extends JpaRepository<Report, Long> {

    @Query(value = """
        SELECT * FROM reports r 
        WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude)) 
        * cos(radians(r.longitude) - radians(:lng)) 
        + sin(radians(:lat)) * sin(radians(r.latitude)))) < :radius
        ORDER BY r.created_at DESC
        """,
            nativeQuery = true)
    List<Report> findReportsWithinRadius(
            @Param("lat") Double latitude,
            @Param("lng") Double longitude,
            @Param("radius") Integer radius
    );

    @Query(value = """
        SELECT * FROM reports r 
        WHERE (6371 * acos(cos(radians(:lat)) * cos(radians(r.latitude)) 
        * cos(radians(r.longitude) - radians(:lng)) 
        + sin(radians(:lat)) * sin(radians(r.latitude)))) < :radius
        ORDER BY r.created_at DESC
        LIMIT :limit OFFSET :offset
        """,
            nativeQuery = true)
    List<Report> findReportsWithinRadiusPageable(
            @Param("lat") Double latitude,
            @Param("lng") Double longitude,
            @Param("radius") Integer radius,
            @Param("limit") Integer limit,
            @Param("offset") Integer offset
    );

    Page<Report> findByCategory(Category category, Pageable pageable);

    Page<Report> findByStatus(ReportStatus status, Pageable pageable);

    @Query("SELECT r FROM Report r WHERE " +
            "(:category IS NULL OR r.category = :category) AND " +
            "(:status IS NULL OR r.status = :status)")
    Page<Report> findByFilters(
            @Param("category") Category category,
            @Param("status") ReportStatus status,
            Pageable pageable
    );

    Page<Report> findByCreatedBy(User user, Pageable pageable);

    List<Report> findByPostalCode(String postalCode);

    @Query("SELECT r FROM Report r WHERE r.createdAt > :since ORDER BY r.createdAt DESC")
    List<Report> findRecentReports(@Param("since") java.time.LocalDateTime since);
}
