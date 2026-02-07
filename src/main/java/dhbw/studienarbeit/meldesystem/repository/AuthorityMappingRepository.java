package dhbw.studienarbeit.meldesystem.repository;

import dhbw.studienarbeit.meldesystem.model.AuthorityMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import dhbw.studienarbeit.meldesystem.model.*;

import java.util.List;
import java.util.Optional;

@Repository
public interface AuthorityMappingRepository extends JpaRepository<AuthorityMapping, Long> {

    @Query("SELECT am FROM AuthorityMapping am WHERE am.category = :category " +
            "AND (am.postalCode IS NULL OR am.postalCode = :postalCode) " +
            "ORDER BY am.postalCode DESC NULLS LAST")
    List<AuthorityMapping> findByCategoryAndPostalCode(
            @Param("category") Category category,
            @Param("postalCode") String postalCode
    );

    @Query("SELECT am FROM AuthorityMapping am WHERE am.category = :category " +
            "AND (am.postalCode IS NULL OR am.postalCode = :postalCode) " +
            "ORDER BY am.postalCode DESC NULLS LAST LIMIT 1")
    Optional<AuthorityMapping> findFirstByCategoryAndPostalCode(
            @Param("category") Category category,
            @Param("postalCode") String postalCode
    );

    List<AuthorityMapping> findByCategory(Category category);
}