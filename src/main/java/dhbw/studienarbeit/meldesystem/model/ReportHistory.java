package dhbw.studienarbeit.meldesystem.model;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "report_history")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReportHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "report_id", nullable = false)
    private Report report;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReportStatus status;

    @Column(name = "changed_by")
    private String changedBy; // System, User nickname, oder Authority name

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(length = 1000)
    private String comment;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}