package dhbw.studienarbeit.meldesystem.repository;


import dhbw.studienarbeit.meldesystem.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    Boolean existsByEmail(String email);
    Boolean existsByNickname(String nickname);

    @Query("SELECT u FROM User u WHERE u.fcmToken IS NOT NULL")
    List<User> findAllWithFcmToken();
}