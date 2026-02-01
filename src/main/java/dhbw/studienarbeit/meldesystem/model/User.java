package dhbw.studienarbeit.meldesystem.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String nickname;

    @Column(nullable = false)
    private Boolean isAnonymous = false;

    private Boolean verified = false;

    private String phoneNumber;

    @Column(name = "notification_radius")
    private Integer notificationRadius = 5;

    @Column(name = "fcm_token")
    private String fcmToken;

    @Column(name = "language_preference")
    private String languagePreference = "DE";

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @OneToMany(mappedBy = "createdBy", cascade = CascadeType.ALL)
    private Set<Report> reports = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}